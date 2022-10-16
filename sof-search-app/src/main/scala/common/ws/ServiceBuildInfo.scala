package common.ws

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.generic.auto.SchemaDerivation

case class ServiceBuildInfo(name: String, version: String, buildTime: String, commit: Option[String])

object ServiceBuildInfo extends SchemaDerivation {
  implicit val buildInfoCodec: Codec[ServiceBuildInfo] = deriveCodec
}
