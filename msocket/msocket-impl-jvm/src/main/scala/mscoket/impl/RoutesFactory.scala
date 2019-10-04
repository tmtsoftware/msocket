package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import io.bullet.borer.{Decoder, Json}
import mscoket.impl.sse.QueryHeader
import mscoket.impl.ws.WsServerFlow
import msocket.api.MessageHandler

class RoutesFactory[Req: Decoder](
    postHandler: MessageHandler[Req, Route],
    websocketHandler: MessageHandler[Req, Source[Message, NotUsed]],
    sseHandler: MessageHandler[Req, Route]
)(implicit mat: Materializer)
    extends HttpCodecs {

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
        entity(as[Req])(postHandler.handle)
      }
    }
  }

  private def extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => Json.decode(query.getBytes()).to[Req].value
  }
}
