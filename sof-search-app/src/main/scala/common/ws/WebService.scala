package common.ws

import cats.effect.Async
import cats.implicits._
import common.ws.WS.WSEndpoint
import org.http4s._
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.internal.RichEndpointInput
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler.default
import sttp.tapir.swagger.SwaggerUI

case class WebService[F[_]: Async](
    info: ServiceBuildInfo,
    controllers: Seq[Controller[F]]
) {

  def getSocketBuilderToApp(wbs: WebSocketBuilder2[F]): HttpRoutes[F] = {

    val (adminEndpoints, businessEndpoints) = controllers.partition(_.name == "Admin") match {
      case (admControllers, busControllers) =>
        (
          admControllers.flatMap(hangTag),
          busControllers.flatMap(hangTag)
        )
    }

    val endpointsCommon = getServiceEndpoints ++ businessEndpoints

    val serverOptions = Http4sServerOptions.default

    def allRoutes(wbs: WebSocketBuilder2[F]) = Http4sServerInterpreter(serverOptions)
      .toWebSocketRoutes(serverEndpoints = endpointsCommon ++ adminEndpoints.toList)(wbs) <+> docsRoutes(
      endpointsCommon
    )

    Router[F]("/" -> allRoutes(wbs), "admin/" -> docsRoutes(adminEndpoints))

  }

  private def docsRoutes(endpoints: Seq[WSEndpoint[F]]): HttpRoutes[F] = {
    val docs = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(
      ses = endpoints,
      title = info.name,
      version = info.version
    )
    Http4sServerInterpreter().toRoutes(SwaggerUI[F](docs.toYaml))
  }

  private def hangTag(c: Controller[F]): Seq[WSEndpoint[F]] =
    c.endpoints.map(e => Option(c.name).filter(_.nonEmpty).fold(e)(e.tag))

  private def getServiceEndpoints: List[ServerEndpoint[Any, F]] = {
    val infoEndpoint = endpoint
      .in("info")
      .out(jsonBody[ServiceBuildInfo])
      .serverLogicSuccess(_ => info.pure[F])

    List(infoEndpoint).map(_.tag("Service"))
  }
}
