package msocket.http.sse

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.headers.AppNameHeader
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

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = SseMetrics.gauge()
    lazy val perMsgCounter = SseMetrics.counter()

    get {
      path(endpoint) {
        parameters(AppNameHeader.name.optional) { appName: Option[String] =>
          extractPayloadFromHeader { req =>
            extractClientIP { clientIp =>
              val collector = new MetricCollector(metricsEnabled, req, clientIp.toString(), appName, Some(perMsgCounter), Some(gauge))
              complete(sseResponseEncoder.encodeStream(streamRequestHandler.handle(req), collector))
            }
          }
        }
      }
    }
  }
}
