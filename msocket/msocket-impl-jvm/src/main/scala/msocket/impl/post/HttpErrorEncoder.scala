package msocket.impl.post

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.impl.MessageEncoder

class HttpErrorEncoder[Req: ErrorProtocol] extends MessageEncoder[Req, Route] with ServerHttpCodecs {
  override def encode[Res: Encoder](response: Res): Route = complete(StatusCodes.InternalServerError -> response)
}
