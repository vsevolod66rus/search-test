package common.models.errors

sealed trait ServerError extends Throwable {
  def message: String
  override def getMessage: String = message
}

sealed trait ServerErrorWithCause extends ServerError {
  def cause: Throwable
}

object ServerError {
  final case class TooManyRequestsError(message: String) extends ServerError

  final case class RequestTimeoutError(cause: Throwable) extends ServerErrorWithCause {
    override def message: String = "service request timeout error"
  }

  final case class NotFoundError(cause: Throwable) extends ServerErrorWithCause {
    override def message: String = "not found"
  }
}
