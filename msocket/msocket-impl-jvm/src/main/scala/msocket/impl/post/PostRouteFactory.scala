package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.{MSocketDirectives, RouteFactory}

class PostRouteFactory[Req: Decoder: ErrorProtocol](endpoint: String, postHandler: MessageHandler[Req, Route])
    extends RouteFactory
    with ServerHttpCodecs {

  def make(): Route = {
    post {
      path(endpoint) {
        MSocketDirectives.withAcceptHeader {
          MSocketDirectives.withExceptionHandler[Req].apply {
            entity(as[Req])(postHandler.handle)
          }
        }
      }
    }
  }

}
