package common.endpoints

import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.generic.auto.SchemaDerivation

import scala.util.control.NoStackTrace

case class EndpointError(
    status: StatusCode,
    statusText: String,
    message: Option[String] = None
) extends NoStackTrace {

  override def getMessage: String = statusText + message.fold("")(": " + _)

  override def toString: String =
    s"EndpointError(status=" + status + ", statusText=" + statusText + "message=" + message + ")"
}

object EndpointError extends SchemaDerivation {
  implicit val scEncoder: Encoder[StatusCode] = Encoder[Int].contramap[StatusCode](_.code)
  implicit val scDecoder: Decoder[StatusCode] = Decoder[Int].map(StatusCode(_))
  implicit val codec: Codec[EndpointError]    = deriveCodec

  def notFound(message: String): EndpointError = EndpointError(StatusCode.NotFound, "NotFound", Some(message))

  def tooManyRequests(message: String): EndpointError =
    EndpointError(StatusCode.TooManyRequests, "TooManyRequests", Some(message))

  def requestTimeout(message: String): EndpointError =
    EndpointError(StatusCode.RequestTimeout, "RequestTimeout", Some(message))

  def internalServerError(message: String): EndpointError =
    EndpointError(StatusCode.InternalServerError, "InternalServerError", Some(message))

}
