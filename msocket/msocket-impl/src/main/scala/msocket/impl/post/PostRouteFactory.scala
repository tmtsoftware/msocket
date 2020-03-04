package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.HttpMetrics
import msocket.impl.post.PostDirectives.withAcceptHeader

class PostRouteFactory[Req: Decoder: ErrorProtocol: Labelled](endpoint: String, postHandler: HttpPostHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with HttpMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val counter = httpCounter

    post {
      path(endpoint) {
        withAcceptHeader {
          withExceptionHandler {
            extractClientIP { clientIp =>
              withMetricMetadata(metricsEnabled, counter, clientIp) { metadata =>
                withHttpMetrics(metadata, postHandler.handle)
              }
            }
          }
        }
      }
    }
  }

}
