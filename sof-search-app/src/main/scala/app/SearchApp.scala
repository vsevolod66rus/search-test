package app

import buildinfo.BuildInfo
import app.controllers.SearchController
import app.services.{ErrorHandlerService, SearchService}
import cats.effect.{Async, Resource}
import common.client.Http4sClient
import common.models.configs.ApplicationConfig
import common.ws.{ServiceBuildInfo, WSApp, WebService}
import org.typelevel.log4cats.Logger
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource

object SearchApp extends WSApp[ApplicationConfig] {

  override def service[F[+_]: Async: Logger]: Resource[F, WebService[F]] = for {
    // Common
    conf   <- Resource.eval(ConfigSource.default.loadF[F, ApplicationConfig]())
    client <- Http4sClient[F](conf.clientConfig)

    // Services
    errorService  <- ErrorHandlerService[F]
    searchService <- SearchService[F](client, conf.serviceConfig, conf.searchConfig)

    // Controllers
    searchController <- SearchController[F](searchService, errorService)

  } yield WebService[F](buildServiceInfo, Seq(searchController))

  private val buildServiceInfo = ServiceBuildInfo(
    name = BuildInfo.name,
    version = BuildInfo.version,
    buildTime = BuildInfo.buildTime,
    commit = BuildInfo.commit
  )
}
