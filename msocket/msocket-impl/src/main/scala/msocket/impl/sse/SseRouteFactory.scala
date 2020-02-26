package msocket.impl.sse

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.impl.RouteFactory
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import msocket.api.Labelled

class SseRouteFactory[Req: Decoder](endpoint: String, sseHandler: SseHandler[Req]) extends RouteFactory[Req] {

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  override def make(labelNames: List[String] = List.empty, metricsEnabled: Boolean = false)(implicit labelGen: Req => Labelled[Req]): Route = {
    get {
      path(endpoint) {
        extractPayloadFromHeader { streamReq =>
          complete(sseHandler.handle(streamReq))
        }
      }
    }
  }
}
