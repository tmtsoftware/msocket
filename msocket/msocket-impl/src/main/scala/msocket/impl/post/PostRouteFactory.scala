package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.impl.RouteFactory

class PostRouteFactory[Req: Decoder: ErrorProtocol](endpoint: String, postHandler: HttpPostHandler[Req])
    extends RouteFactory
    with ServerHttpCodecs {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(): Route = {
    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          withExceptionHandler {
            entity(as[Req])(postHandler.handle)
          }
        }
      }
    }
  }

}
