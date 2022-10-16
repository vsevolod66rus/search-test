package common.types

import common.models.logic.ExternalSearchResult

object SearchTypes {
  type SearchResponse = Map[String, ExternalSearchResult]
}
