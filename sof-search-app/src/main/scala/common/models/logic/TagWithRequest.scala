package common.models.logic

import org.http4s.Request

case class TagWithRequest[F[_]](tag: String, req: Request[F])
