package msocket.http.sse

import org.apache.pekko.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import org.apache.pekko.http.scaladsl.server.Directives.{complete, _}
import org.apache.pekko.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.PostDirectives
import msocket.http.post.headers.{AppNameHeader, UserNameHeader}
import msocket.jvm.metrics.{LabelExtractor, MetricCollector}
import msocket.jvm.stream.StreamRequestHandler
import msocket.security.AccessControllerFactory

import scala.concurrent.ExecutionContext

class SseRouteFactory[Req: Decoder: ErrorProtocol: LabelExtractor](
    endpoint: String,
    streamRequestHandler: StreamRequestHandler[Req],
    accessControllerFactory: AccessControllerFactory
)(implicit ec: ExecutionContext)
    extends RouteFactory[Req] {

  private val sseResponseEncoder = new SseStreamResponseEncoder[Req](accessControllerFactory.make(None))

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = SseMetrics.gauge()
    lazy val perMsgCounter = SseMetrics.counter()

    post {
      path(endpoint) {
        optionalHeaderValueByName(AppNameHeader.name) { appName =>
          optionalHeaderValueByName(UserNameHeader.name) { username =>
            withExceptionHandler {
              entity(as[String]) { request =>
                val req       = JsonText.decode[Req](request)
                val collector =
                  new MetricCollector(metricsEnabled, req, appName, username, Some(perMsgCounter), Some(gauge))
                complete(sseResponseEncoder.encodeStream(streamRequestHandler.handle(req), collector))
              }
            }
          }
        }
      }
    }
  }
}
