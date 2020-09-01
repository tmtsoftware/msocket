package msocket.impl.post

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import io.bullet.borer.Encoder
import msocket.api.models.Headers
import msocket.api.{ErrorProtocol, MessageEncoder}
import msocket.impl.post.headers.ErrorTypeHeader

class HttpErrorEncoder[Req: ErrorProtocol] extends MessageEncoder[Req, Route] with ServerHttpCodecs {
  override def encode[Res: Encoder](response: Res, headers: Headers): Route = {

    val responseHeaders = headers.errorType match {
      case Some(value) => Seq(ErrorTypeHeader(value.toString))
      case _           => Seq.empty
    }

    complete(StatusCodes.InternalServerError, responseHeaders, response)
  }
}
