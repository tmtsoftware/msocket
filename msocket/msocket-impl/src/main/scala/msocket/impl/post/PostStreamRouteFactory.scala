package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.PostStreamMetrics
import msocket.impl.post.PostDirectives.withAcceptHeader

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol: Labelled](endpoint: String, postHandler: HttpStreamHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with PostStreamMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = postStreamGauge
    lazy val perMsgCounter = postStreamPerMsgCounter

    post {
      path(endpoint) {
        withAcceptHeader {
          withExceptionHandler {
            entity(as[Req]) { req =>
              withMetricCollector(metricsEnabled, req, counter = Some(perMsgCounter), gauge = Some(gauge)).apply { collector =>
                complete(withMetrics(postHandler.handle(req), collector))
              }
            }
          }
        }
      }
    }
  }
}
