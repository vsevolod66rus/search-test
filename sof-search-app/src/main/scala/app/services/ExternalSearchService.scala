package app.services

import cats.effect.{Async, Resource, Sync}
import cats.implicits._
import org.typelevel.log4cats.Logger
import fs2.Stream
import org.http4s.Method.GET
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{Request, Uri}
import org.http4s.client.Client
import common.models.configs.{SearchConfig, ServiceConfig}
import common.models.logic._
import common.types.SearchTypes.SearchResponse

trait ExternalSearchService[F[_]] {

  def handleSearch(tags: List[String]): F[SearchResponse]
}

object ExternalSearchService {

  def apply[F[+_]: Async: Logger](
      client: Client[F],
      serviceConfig: ServiceConfig,
      searchConfig: SearchConfig
  ): Resource[F, ExternalSearchService[F]] =
    Resource.eval(Sync[F].delay(new ExternalSearchServiceImpl(client, serviceConfig, searchConfig)))
}

class ExternalSearchServiceImpl[F[+_]: Async: Logger](
    client: Client[F],
    serviceConfig: ServiceConfig,
    searchConfig: SearchConfig
) extends ExternalSearchService[F] {

  def handleSearch(tags: List[String]): F[SearchResponse] = for {
    requests <- tags.map(buildExternalSearchRequest).pure[F]
    res      <- Stream
                  .emits[F, TagWithRequest[F]](requests)
                  .parEvalMap(serviceConfig.maxParallelExternalSearch)(fetchExternalSearchResponse)
                  .compile
                  .toList
  } yield res.toMap

  private def buildExternalSearchRequest(tag: String): TagWithRequest[F] = {
    val uri                = Uri.unsafeFromString(s"${searchConfig.uri}")
    val uriWithQueryParams =
      searchConfig.queryParams
        .foldLeft(uri)((acc, el) => acc.withQueryParam(el._1, el._2))
        .withQueryParam(searchConfig.tagParam, tag)
    TagWithRequest(tag, Request[F](method = GET, uri = uriWithQueryParams))
  }

  private def fetchExternalSearchResponse(tagWithReq: TagWithRequest[F]): F[(String, ExternalSearchResult)] = for {
    resEither <- client.fetchAs[ExternalSearchResponse](tagWithReq.req).attempt
    res       <- resEither match {
                   case Right(response) =>
                     val total    = response.items.count(_.tags.contains(tagWithReq.tag))
                     val answered = response.items.count(_.is_answered)
                     TagInfo(total = total, answered = answered).pure[F]
                   case Left(e)         =>
                     Logger[F]
                       .error(s"tag=${tagWithReq.tag}:\n${e.getMessage}")
                       .map(_ => ExternalSearchError("An error occurred while perform the search"))
                 }
    _         <- Logger[F].info(s"tag=${tagWithReq.tag} was searched by uri=${tagWithReq.req.uri} with result: $res")
  } yield (tagWithReq.tag, res)
}
