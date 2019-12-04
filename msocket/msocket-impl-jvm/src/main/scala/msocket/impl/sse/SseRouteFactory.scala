package msocket.impl.sse

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.MessageHandler
import msocket.impl.{MSocketDirectives, RouteFactory}

import scala.reflect.ClassTag

class SseRouteFactory[Req: Decoder, Err <: Throwable: Encoder: ClassTag](endpoint: String, sseHandler: MessageHandler[Req, Route])
    extends RouteFactory {

  def make(): Route = {
    get {
      path(endpoint) {
        MSocketDirectives.withExceptionHandler[Err].apply {
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
