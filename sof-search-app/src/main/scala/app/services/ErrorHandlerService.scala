package app.services

import cats.effect.Sync
import cats.effect.kernel.Resource
import cats.implicits.{catsSyntaxApplicativeError, toFunctorOps}
import common.endpoints.EndpointError
import common.models.errors.ServerError._
import common.models.errors.ServerErrorWithCause
import org.typelevel.log4cats.Logger

trait ErrorHandlerService[F[_]] {

  def handleErrorsWithLogging[A](errorMessage: String)(effect: F[A]): F[Either[EndpointError, A]]

  protected def refineToEndpointError(errorMessage: String): PartialFunction[Throwable, EndpointError] = {
    case _: TooManyRequestsError => EndpointError.tooManyRequests(ErrorMessage.tooManyRequests)
    case e: RequestTimeoutError  => EndpointError.requestTimeout(e.message)
    case e: NotFoundError        => EndpointError.notFound(e.message)
    case _                       => EndpointError.internalServerError(errorMessage)
  }

  private object ErrorMessage {

    val tooManyRequests: String = "На данный момент выполняется максимальное количество запросов. " +
      "Пожалуйста, попробуйте выполнить Ваш запрос через несколько секунд."
  }
}

object ErrorHandlerService {

  def apply[F[_]: Sync: Logger]: Resource[F, ErrorHandlerService[F]] =
    Resource.eval(Sync[F].delay(new ErrorHandlerServiceImpl[F]))
}

class ErrorHandlerServiceImpl[F[_]: Sync: Logger] extends ErrorHandlerService[F] {

  override def handleErrorsWithLogging[A](
      errorMessage: String
  )(effect: F[A]): F[Either[EndpointError, A]] =
    for {
      resultEither <- effect.onError(logErrorWith(errorMessage)).attempt
      result        = resultEither.left.map(refineToEndpointError(errorMessage))
    } yield result

  private def logErrorWith[E <: Throwable](errorMessage: String): PartialFunction[E, F[Unit]] = {
    case e: ServerErrorWithCause => Logger[F].error(e.cause)(s"$errorMessage\n${e.message}")
    case e                       => Logger[F].error(e)(s"$errorMessage")
  }

}
