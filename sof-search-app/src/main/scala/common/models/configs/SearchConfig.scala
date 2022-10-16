package common.models.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class SearchConfig(uri: String, queryParams: Map[String, String], tagParam: String)

object SearchConfig {
  implicit val reader: ConfigReader[SearchConfig] = deriveReader
}
