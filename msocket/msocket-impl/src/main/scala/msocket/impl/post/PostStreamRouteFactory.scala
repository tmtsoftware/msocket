package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labelled}
import msocket.impl.RouteFactory

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol](endpoint: String, postHandler: HttpStreamHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  override def make(labelNames: List[String] = List.empty, metricsEnabled: Boolean = false)(implicit labelGen: Req => Labelled[Req]): Route = {
    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          withExceptionHandler {
            entity(as[Req])(x => complete(postHandler.handle(x)))
          }
        }
      }
    }
  }
}
