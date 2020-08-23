package msocket.impl.sse

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ErrorProtocol, Labelled, StreamRequestHandler}
import msocket.impl.RouteFactory
import msocket.impl.metrics.SseMetrics

class SseRouteFactory[Req: Decoder: ErrorProtocol: Labelled](endpoint: String, streamRequestHandler: StreamRequestHandler[Req])
    extends RouteFactory[Req]
    with SseMetrics {

  private val sseHandler = new SseHandler[Req]

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = sseGauge
    lazy val perMsgCounter = ssePerMsgCounter

    get {
      path(endpoint) {
        extractPayloadFromHeader { req =>
          withMetricCollector(metricsEnabled, req, counter = Some(perMsgCounter), gauge = Some(gauge)).apply { collector =>
            complete(sseHandler.handle(streamRequestHandler.handle(req), collector))
          }
        }
      }
    }
  }
}
