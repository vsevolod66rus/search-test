package common.models.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class ServiceConfig(maxParallelInternalSearch: Int, maxParallelExternalSearch: Int)

object ServiceConfig {
  implicit val reader: ConfigReader[ServiceConfig] = deriveReader
}
