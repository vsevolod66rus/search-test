package app.services

import cats.effect.std.Semaphore
import cats.effect.{Async, Resource}
import cats.implicits._
import org.typelevel.log4cats.Logger
import common.models.configs.ServiceConfig
import common.models.errors.ServerError.TooManyRequestsError
import common.types.SearchTypes.SearchResponse

trait SearchService[F[_]] {
  def sofSearch(tags: List[String]): F[SearchResponse]
}

object SearchService {

  def apply[F[+_]: Async: Logger](
      externalSearchService: ExternalSearchService[F],
      serviceConfig: ServiceConfig
  ): Resource[F, SearchService[F]] =
    Resource.eval(
      Semaphore[F](serviceConfig.maxParallelInternalSearch.toLong)
        .map(new SearchServiceImpl(_, externalSearchService, serviceConfig))
    )
}

class SearchServiceImpl[F[+_]: Async: Logger](
    internalSemaphore: Semaphore[F],
    externalSearchService: ExternalSearchService[F],
    serviceConfig: ServiceConfig
) extends SearchService[F] {

  def sofSearch(tags: List[String]): F[SearchResponse] =
    internalSemaphore.tryAcquire.ifM(
      for {
        count  <- internalSemaphore.count
        _      <- Logger[F].info(s"Internal semaphore count = $count")
        answer <- Async[F].guarantee(externalSearchService.handleSearch(tags.distinct), internalSemaphore.release)
      } yield answer,
      Async[F].raiseError[SearchResponse](
        TooManyRequestsError(s"Too many requests error (${serviceConfig.maxParallelInternalSearch}) error occurred")
      )
    )

}
