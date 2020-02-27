package msocket.impl.sse

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{LabelNames, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.SseMetrics

class SseRouteFactory[Req: Decoder: LabelNames: Labelled](endpoint: String, sseHandler: SseHandler[Req])
    extends RouteFactory[Req]
    with SseMetrics {

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge = sseGauge

    get {
      path(endpoint) {
        extractPayloadFromHeader { streamReq =>
          withMetricMetadata(metricsEnabled, gauge) { metadata =>
            val source = sseHandler.handle(streamReq)
            complete(withMetrics(source, streamReq, metadata))
          }
        }
      }
    }
  }
}
