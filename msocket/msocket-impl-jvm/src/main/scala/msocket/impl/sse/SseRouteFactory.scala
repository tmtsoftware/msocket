package msocket.impl.sse

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.{Decoder, Json}
import msocket.api.MessageHandler
import msocket.impl.{MSocketDirectives, RouteFactory}

class SseRouteFactory[Req: Decoder](endpoint: String, sseHandler: MessageHandler[Req, Route]) extends RouteFactory {

  def make(): Route = {
    get {
      path(endpoint) {
        MSocketDirectives.withExceptionHandler {
          extractPayloadFromHeader { streamReq =>
            sseHandler.handle(streamReq)
          }
        }
      }
    }
  }

  private def extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => Json.decode(query.getBytes()).to[Req].value
  }
}
