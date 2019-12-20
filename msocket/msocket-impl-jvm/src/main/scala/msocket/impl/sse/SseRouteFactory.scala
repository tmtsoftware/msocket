package msocket.impl.sse

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.Decoder
import msocket.api.Encoding.JsonText
import msocket.impl.RouteFactory

class SseRouteFactory[Req: Decoder](endpoint: String, sseHandler: SseHandler[Req]) extends RouteFactory {

  private val extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => JsonText.decode(query)
  }

  def make(): Route = {
    get {
      path(endpoint) {
        extractPayloadFromHeader { streamReq =>
          sseHandler.handle(streamReq)
        }
      }
    }
  }
}
