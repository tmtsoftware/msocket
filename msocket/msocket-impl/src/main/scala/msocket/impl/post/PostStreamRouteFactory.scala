package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, LabelNames, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.{MetricMetadata, PostStreamMetrics}

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol: LabelNames](endpoint: String, postHandler: HttpStreamHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with PostStreamMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false)(implicit labelGen: Req => Labelled[Req]): Route = {
    lazy val gauge = postStreamGauge(LabelNames[Req].get)

    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          withExceptionHandler {
            extractExecutionContext { implicit ec =>
              extractHost { address =>
                entity(as[Req]) { req =>
                  complete {
                    val metadata = MetricMetadata(metricsEnabled, address, gauge)
                    withMetrics(postHandler.handle(req), req, metadata)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
