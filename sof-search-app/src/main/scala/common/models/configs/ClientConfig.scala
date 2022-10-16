package common.models.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class ClientConfig(
    idleTimeout: Int,
    requestTimeout: Int
)

object ClientConfig {
  implicit val reader: ConfigReader[ClientConfig] = deriveReader
}
