package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, LabelNames, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.{HttpMetrics, MetricMetadata}

class PostRouteFactory[Req: Decoder: ErrorProtocol: LabelNames](endpoint: String, postHandler: HttpPostHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with HttpMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false)(implicit labelGen: Req => Labelled[Req]): Route = {
    lazy val counter = httpCounter(LabelNames[Req].get)
    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          withExceptionHandler {
            extractHost { address =>
              val metadata = MetricMetadata(metricsEnabled, address, counter)
              withHttpMetrics(metadata, postHandler.handle)
            }
          }
        }
      }
    }
  }

}
