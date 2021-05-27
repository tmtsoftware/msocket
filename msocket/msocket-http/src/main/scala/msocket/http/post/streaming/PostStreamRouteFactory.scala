package msocket.http.post.streaming

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.PostDirectives.withAcceptHeader
import msocket.http.post.headers.{AppNameHeader, UserNameHeader}
import msocket.http.post.{PostDirectives, ServerHttpCodecs}
import msocket.jvm.metrics.{LabelExtractor, MetricCollector}
import msocket.jvm.stream.StreamRequestHandler
import msocket.security.AccessControllerFactory

import scala.concurrent.ExecutionContext

class PostStreamRouteFactory[Req: Decoder: ErrorProtocol: LabelExtractor](
    endpoint: String,
    streamRequestHandler: StreamRequestHandler[Req],
    accessControllerFactory: AccessControllerFactory
)(implicit ec: ExecutionContext)
    extends RouteFactory[Req]
    with ServerHttpCodecs {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]
  private val streamResponseEncoder            = new HttpStreamResponseEncoder[Req](accessControllerFactory.make(None))

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = PostStreamMetrics.gauge()
    lazy val perMsgCounter = PostStreamMetrics.counter()

    post {
      path(endpoint) {
        optionalHeaderValueByName(AppNameHeader.name) { appName =>
          optionalHeaderValueByName(UserNameHeader.name) { username =>
            withAcceptHeader {
              withExceptionHandler {
                entity(as[Req]) { req =>
                  extractClientIP { clientIp =>
                    val collector =
                      new MetricCollector(metricsEnabled, req, clientIp.toString(), appName, username, Some(perMsgCounter), Some(gauge))
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
}
