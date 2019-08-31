package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route, StandardRoute}
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.RequestHandler
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import mscoket.impl.sse.QueryHeader
import mscoket.impl.ws.WsServerFlow

class RoutesFactory[PostReq: Decoder, StreamReq: Encoder: Decoder](
    postHandler: RequestHandler[PostReq, StandardRoute],
    websocketHandler: RequestHandler[StreamReq, Source[Message, NotUsed]],
    sseHandler: RequestHandler[StreamReq, StandardRoute]
) extends HttpCodecs {

  val route: Route = cors() {
    get {
      path("websocket") {
        handleWebSocketMessages {
          new WsServerFlow(websocketHandler).flow
        }
      } ~
      path("sse") {
        extractPayloadFromHeader { streamReq =>
          sseHandler.handle(streamReq)
        }
      }
    } ~
    post {
      path("post") {
        entity(as[PostReq])(postHandler.handle)
      }
    }
  }

  private def extractPayloadFromHeader: Directive1[StreamReq] = headerValuePF {
    case QueryHeader(query) => Json.decode(query.getBytes()).to[StreamReq].value
  }
}
