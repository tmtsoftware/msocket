package msocket.http.post.streaming

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.PostDirectives.withAcceptHeader
import msocket.http.post.headers.AppNameHeader
import msocket.http.post.{PostDirectives, ServerHttpCodecs}
import msocket.jvm.metrics.{Labelled, MetricCollector}
import msocket.jvm.stream.StreamRequestHandler
import msocket.security.AccessControllerFactory

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
  private val streamResponseEncoder            = new HttpStreamResponseEncoder[Req](accessControllerFactory.make(None))

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
                  complete(streamResponseEncoder.encodeStream(streamRequestHandler.handle(req), collector))
                }
              }
            }
          }
        }
      }
    }
  }
}
