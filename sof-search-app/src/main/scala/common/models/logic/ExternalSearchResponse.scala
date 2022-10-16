package common.models.logic

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema

case class ExternalSearchResponse(items: List[SearchItem])

case class SearchItem(tags: List[String], is_answered: Boolean)

object ExternalSearchResponse {
  implicit val codec: Codec[ExternalSearchResponse]   = deriveCodec
  implicit val schema: Schema[ExternalSearchResponse] = Schema.derived
}

object SearchItem {
  implicit val codec: Codec[SearchItem]   = deriveCodec
  implicit val schema: Schema[SearchItem] = Schema.derived
}
