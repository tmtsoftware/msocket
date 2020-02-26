package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.HttpMetrics

class PostRouteFactory[Req: Decoder: ErrorProtocol](endpoint: String, postHandler: HttpPostHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with HttpMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(labelNames: List[String] = List.empty, metricsEnabled: Boolean = false)(implicit labelGen: Req => Labelled[Req]): Route = {
    lazy val counter = httpCounter(labelNames)

    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          withExceptionHandler {
            routeMetrics(metricsEnabled, counter)(postHandler.handle)
          }
        }
      }
    }
  }

}
