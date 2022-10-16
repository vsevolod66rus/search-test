package common.models.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class ApplicationConfig(
    clientConfig: ClientConfig,
    serviceConfig: ServiceConfig,
    searchConfig: SearchConfig
)

object ApplicationConfig {
  implicit val reader: ConfigReader[ApplicationConfig] = deriveReader
}
