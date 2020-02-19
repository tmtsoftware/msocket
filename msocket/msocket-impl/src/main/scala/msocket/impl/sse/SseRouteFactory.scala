package msocket.impl.sse

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.impl.RouteFactory
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream

class SseRouteFactory[Req: Decoder](endpoint: String, sseHandler: SseHandler[Req]) extends RouteFactory {

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  def make(): Route = {
    get {
      path(endpoint) {
        extractPayloadFromHeader { streamReq =>
          complete(sseHandler.handle(streamReq))
        }
      }
    }
  }
}
