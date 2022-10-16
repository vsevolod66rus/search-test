package common.models.logic

import cats.implicits.toFunctorOps
import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax.EncoderOps
import sttp.tapir.Schema

sealed trait ExternalSearchResult extends Product

case class TagInfo(total: Int, answered: Int) extends ExternalSearchResult

case class ExternalSearchError(message: String) extends ExternalSearchResult

object TagInfo {
  implicit val codec: Codec[TagInfo]   = deriveCodec
  implicit val schema: Schema[TagInfo] = Schema.derived
}

object ExternalSearchError {
  implicit val codec: Codec[ExternalSearchError]   = deriveCodec
  implicit val schema: Schema[ExternalSearchError] = Schema.derived
}

object ExternalSearchResult {

  implicit val encodeExternalSearchResult: Encoder[ExternalSearchResult] = Encoder.instance {
    case tagInfo @ TagInfo(_, _)     => tagInfo.asJson
    case _ @ExternalSearchError(msg) => msg.asJson
  }

  implicit val decodeExternalSearchResult: Decoder[ExternalSearchResult] =
    List[Decoder[ExternalSearchResult]](
      Decoder[TagInfo].widen,
      Decoder[ExternalSearchError].widen
    ).reduceLeft(_ or _)

  implicit val schema: Schema[ExternalSearchError] = Schema.derived
}
