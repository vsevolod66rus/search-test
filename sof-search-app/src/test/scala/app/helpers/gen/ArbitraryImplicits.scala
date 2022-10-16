package app.helpers.gen

import org.scalacheck.{Arbitrary, Gen}
import common.models.logic.{ExternalSearchError, ExternalSearchResult, TagInfo}
import common.types.SearchTypes.SearchResponse

object ArbitraryImplicits {
  implicit lazy val arbString: Arbitrary[String]     = Arbitrary(Gen.alphaStr)
  implicit lazy val arbTags: Arbitrary[List[String]] = arbList[String]

  implicit lazy val arbExternalSearchResult: Arbitrary[ExternalSearchResult] = Arbitrary(
    Gen.oneOf(genTagInfo, genExternalSearchError)
  )

  implicit lazy val arbSearchResponse: Arbitrary[Arbitrary[SearchResponse]] = Arbitrary(
    arbMap[ExternalSearchResult]
  )

  implicit lazy val genTagInfo: Gen[TagInfo] =
    for {
      total    <- Gen.size
      answered <- Gen.size
    } yield TagInfo(total, answered)

  implicit lazy val genExternalSearchError: Gen[ExternalSearchError] = Gen.alphaStr.map(s => ExternalSearchError(s))

  implicit def arbList[T: Arbitrary]: Arbitrary[List[T]] = Arbitrary(
    Gen.chooseNum(0, 3).flatMap(Gen.listOfN(_, implicitly[Arbitrary[T]].arbitrary))
  )

  implicit def arbMap[V: Arbitrary]: Arbitrary[Map[String, V]] = Arbitrary {
    for {
      size   <- Gen.chooseNum(0, 3)
      keys   <- Gen.listOfN(size, Gen.alphaStr)
      values <- Gen.listOfN(size, implicitly[Arbitrary[V]].arbitrary)
    } yield keys.zip(values).toMap
  }
}
