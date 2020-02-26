package msocket.impl.sse

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.Labelled
import msocket.impl.RouteFactory
import msocket.impl.metrics.SseMetrics

class SseRouteFactory[Req: Decoder](endpoint: String, sseHandler: SseHandler[Req]) extends RouteFactory[Req] with SseMetrics {

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  override def make(labelNames: List[String] = List.empty, metricsEnabled: Boolean = false)(
      implicit labelGen: Req => Labelled[Req]
  ): Route = {
    lazy val gauge = sseGauge(labelNames)

    get {
      path(endpoint) {
        extractPayloadFromHeader { streamReq =>
          extractExecutionContext { implicit ec =>
            val source = sseHandler.handle(streamReq)
            sseMetrics(streamReq, source, metricsEnabled, gauge)
          }
        }
      }
    }
  }
}
