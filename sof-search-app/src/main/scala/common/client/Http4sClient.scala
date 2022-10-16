package common.client

import cats.effect.{Async, Resource}
import common.models.configs.ClientConfig
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.client.middleware.GZip

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object Http4sClient {

  def apply[F[_]: Async](config: ClientConfig): Resource[F, Client[F]] = BlazeClientBuilder[F]
    .withExecutionContext(global)
    .withIdleTimeout(config.idleTimeout.minute)
    .withRequestTimeout(config.requestTimeout.minute)
    .resource
    .map(GZip())
}
