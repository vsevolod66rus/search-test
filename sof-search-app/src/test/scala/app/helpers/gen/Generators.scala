package app.helpers.gen

import org.scalacheck.Gen
import ArbitraryImplicits._
import ArbitraryDerivation._
import common.models.logic.ExternalSearchResult
import common.types.SearchTypes.SearchResponse

object Generators {
  lazy val genTags: Gen[List[String]]             = generate[List[String]]
  lazy val genSearchResponse: Gen[SearchResponse] = generate[Map[String, ExternalSearchResult]]
}
