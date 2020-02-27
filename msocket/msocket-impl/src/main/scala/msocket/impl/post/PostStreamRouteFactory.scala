package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, LabelNames, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.PostStreamMetrics
import msocket.impl.post.PostDirectives.withAcceptHeader

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol: LabelNames](endpoint: String, postHandler: HttpStreamHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with PostStreamMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false)(implicit labelGen: Labelled[Req]): Route = {
    lazy val gauge = postStreamGauge(LabelNames[Req].get)

    post {
      path(endpoint) {
        withAcceptHeader {
          withExceptionHandler {
            withMetricMetadata(metricsEnabled, gauge) { metadata =>
              entity(as[Req]) { req =>
                complete(withMetrics(postHandler.handle(req), req, metadata))
              }
            }
          }
        }
      }
    }
  }
}
