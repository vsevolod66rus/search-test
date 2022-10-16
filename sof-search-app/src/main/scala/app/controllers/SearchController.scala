package app.controllers

import app.services.{ErrorHandlerService, SearchService}
import cats.effect.{Async, Resource}
import common.endpoints.EndpointOps.searchEndpoint
import common.ws.Controller
import common.ws.WS.WSEndpoint
import org.typelevel.log4cats.Logger

object SearchController {

  def apply[F[+_]: Async: Logger](
      searchService: SearchService[F],
      errorService: ErrorHandlerService[F]
  ): Resource[F, SearchController[F]] =
    Resource.eval(Async[F].delay(new SearchController[F](searchService, errorService)))
}

class SearchController[F[+_]: Async: Logger](searchService: SearchService[F], errorService: ErrorHandlerService[F])
    extends Controller[F] {

  override val name: String = "Search"

  override val endpoints: Seq[WSEndpoint[F]] = Seq(searchEndpoint.serverLogic { qp =>
    errorService.handleErrorsWithLogging("An error occurred while perform the search") {
      searchService.sofSearch(qp)
    }
  })

}
