package app.helpers.gen

import magnolia1._
import org.scalacheck._

import scala.language.experimental.macros

object ArbitraryDerivation {

  type Typeclass[T] = Arbitrary[T]

  implicit private val monadicGen: Monadic[Gen] = new Monadic[Gen] {
    override def point[A](value: A): Gen[A]                           = Gen.const(value)
    override def map[A, B](from: Gen[A])(fn: A => B): Gen[B]          = from.map(fn)
    override def flatMap[A, B](from: Gen[A])(fn: A => Gen[B]): Gen[B] = from.flatMap(fn)
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]

  def join[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = Arbitrary {
    Gen.lzy(ctx.constructMonadic(_.typeclass.arbitrary))
  }

  def split[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = Arbitrary {
    Gen.oneOf(ctx.subtypes.map(_.typeclass.arbitrary)).flatMap(identity)
  }

  def generate[T](implicit generator: Arbitrary[T]): Gen[T] = generator.arbitrary
}
