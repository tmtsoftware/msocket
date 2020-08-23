package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labelled, StreamRequestHandler}
import msocket.impl.RouteFactory
import msocket.impl.metrics.PostStreamMetrics
import msocket.impl.post.PostDirectives.withAcceptHeader

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol: Labelled](endpoint: String, streamRequestHandler: StreamRequestHandler[Req])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with PostStreamMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]
  private val httpStreamHandler                = new HttpStreamHandler[Req]

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = postStreamGauge
    lazy val perMsgCounter = postStreamPerMsgCounter

    post {
      path(endpoint) {
        withAcceptHeader {
          withExceptionHandler {
            entity(as[Req]) { req =>
              withMetricCollector(metricsEnabled, req, counter = Some(perMsgCounter), gauge = Some(gauge)).apply { collector =>
                complete(httpStreamHandler.handle(streamRequestHandler.handle(req), collector))
              }
            }
          }
        }
      }
    }
  }
}
