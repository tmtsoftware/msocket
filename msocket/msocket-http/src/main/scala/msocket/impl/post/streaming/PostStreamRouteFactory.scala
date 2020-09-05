package msocket.impl.post.streaming

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.impl.RouteFactory
import msocket.impl.post.PostDirectives.withAcceptHeader
import msocket.impl.post.headers.AppNameHeader
import msocket.impl.post.{PostDirectives, ServerHttpCodecs}
import msocket.security.api.AccessControllerFactory
import msocket.service.StreamRequestHandler
import msocket.service.metrics.{Labelled, MetricCollector}

import scala.concurrent.ExecutionContext

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol: Labelled](
    endpoint: String,
    streamRequestHandler: StreamRequestHandler[Req],
    accessControllerFactory: AccessControllerFactory
)(implicit ec: ExecutionContext)
    extends RouteFactory[Req]
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
                extractClientIP { clientIp =>
                  val collector = new MetricCollector(metricsEnabled, req, appName, Some(perMsgCounter), Some(gauge), clientIp.toString())
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
