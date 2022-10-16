package common.ws

import common.ws.WS.WSEndpoint

trait Controller[F[_]] {

  def name: String

  def endpoints: Seq[WSEndpoint[F]]
}
