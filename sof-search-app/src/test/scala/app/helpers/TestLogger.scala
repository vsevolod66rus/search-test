package app.helpers

import cats.Applicative
import cats.effect.Resource
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import org.typelevel.log4cats.Logger

import scala.collection.mutable

final class TestLogger[F[_]: Applicative] extends Logger[F] {
  import TestLogger.LogMessage
  import TestLogger.LogMessage._

  private val messageQueue = mutable.Queue.empty[LogMessage]

  override def error(message: => String): F[Unit]               = messageQueue.enqueue(Error(message)).pure[F].void
  override def warn(message: => String): F[Unit]                = messageQueue.enqueue(Warn(message)).pure[F].void
  override def info(message: => String): F[Unit]                = messageQueue.enqueue(Info(message)).pure[F].void
  override def debug(message: => String): F[Unit]               = messageQueue.enqueue(Debug(message)).pure[F].void
  override def trace(message: => String): F[Unit]               = messageQueue.enqueue(Trace(message)).pure[F].void
  override def error(t: Throwable)(message: => String): F[Unit] = messageQueue.enqueue(Error(message)).pure[F].void
  override def warn(t: Throwable)(message: => String): F[Unit]  = messageQueue.enqueue(Warn(message)).pure[F].void
  override def info(t: Throwable)(message: => String): F[Unit]  = messageQueue.enqueue(Info(message)).pure[F].void
  override def debug(t: Throwable)(message: => String): F[Unit] = messageQueue.enqueue(Debug(message)).pure[F].void
  override def trace(t: Throwable)(message: => String): F[Unit] = messageQueue.enqueue(Trace(message)).pure[F].void

  def dequeueMessage(): F[Option[LogMessage]] = messageQueue.dequeueFirst(_ => true).pure[F]
}

object TestLogger {

  sealed trait LogMessage {
    def message: String
  }

  object LogMessage {
    final case class Error(message: String) extends LogMessage
    final case class Warn(message: String)  extends LogMessage
    final case class Info(message: String)  extends LogMessage
    final case class Debug(message: String) extends LogMessage
    final case class Trace(message: String) extends LogMessage
  }

  def apply[F[_]: Applicative](): TestLogger[F]                = new TestLogger[F]
  def createF[F[_]: Applicative]: F[TestLogger[F]]             = TestLogger().pure[F]
  def createRes[F[_]: Applicative]: Resource[F, TestLogger[F]] = Resource.eval(TestLogger().pure[F])
}
