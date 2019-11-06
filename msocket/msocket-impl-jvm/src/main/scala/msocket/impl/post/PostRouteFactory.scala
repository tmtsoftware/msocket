package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.MessageHandler
import msocket.impl.RouteFactory

class PostRouteFactory[Req: Decoder](endpoint: String, postHandler: MessageHandler[Req, Route]) extends RouteFactory with ServerHttpCodecs {

  def make(): Route = {
    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          entity(as[Req])(postHandler.handle)
        }
      }
    }
  }

}
