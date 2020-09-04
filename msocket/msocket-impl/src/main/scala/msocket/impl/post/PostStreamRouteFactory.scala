package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labelled, StreamRequestHandler}
import msocket.impl.RouteFactory
import msocket.impl.metrics.PostStreamMetrics
import msocket.impl.post.PostDirectives.withAcceptHeader
import msocket.impl.post.headers.AppNameHeader
import msocket.security.api.AccessControllerFactory

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol: Labelled](
    endpoint: String,
    streamRequestHandler: StreamRequestHandler[Req],
    accessControllerFactory: AccessControllerFactory
) extends RouteFactory[Req]
    with ServerHttpCodecs
    with PostStreamMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]
  private val httpStreamHandler                = new HttpStreamResponseEncoder[Req](accessControllerFactory.make(None))

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = postStreamGauge
    lazy val perMsgCounter = postStreamPerMsgCounter

    post {
      path(endpoint) {
        optionalHeaderValueByName(AppNameHeader.name) { appName =>
          withAcceptHeader {
            withExceptionHandler {
              entity(as[Req]) { req =>
                withMetricCollector(metricsEnabled, req, appName, counter = Some(perMsgCounter), gauge = Some(gauge)).apply { collector =>
                  complete(httpStreamHandler.handle(streamRequestHandler.handle(req), collector))
                }
              }
            }
          }
        }
      }
    }
  }
}
