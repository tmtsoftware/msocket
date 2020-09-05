package msocket.http.sse

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.headers.AppNameHeader
import msocket.security.AccessControllerFactory
import msocket.jvm.StreamRequestHandler
import msocket.jvm.metrics.{Labelled, MetricCollector}

import scala.concurrent.ExecutionContext

class SseRouteFactory[Req: Decoder: ErrorProtocol: Labelled](
    endpoint: String,
    streamRequestHandler: StreamRequestHandler[Req],
    accessControllerFactory: AccessControllerFactory
)(implicit ec: ExecutionContext)
    extends RouteFactory[Req]
    with SseMetrics {

  private val sseHandler = new SseStreamResponseEncoder[Req](accessControllerFactory.make(None))

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = sseGauge
    lazy val perMsgCounter = ssePerMsgCounter

    get {
      path(endpoint) {
        parameters(AppNameHeader.name.optional) { appName: Option[String] =>
          extractPayloadFromHeader { req =>
            extractClientIP { clientIp =>
              val collector = new MetricCollector(metricsEnabled, req, appName, Some(perMsgCounter), Some(gauge), clientIp.toString())
              complete(sseHandler.handle(streamRequestHandler.handle(req), collector))
            }
          }
        }
      }
    }
  }
}
