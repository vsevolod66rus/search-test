package common.ws

import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint

object WS {
  type WSEndpoint[F[_]] = ServerEndpoint[Fs2Streams[F] with WebSockets, F]

}
