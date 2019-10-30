package msocket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import io.bullet.borer.{Decoder, Json}
import msocket.api.MessageHandler
import msocket.impl.post.{PostDirectives, ServerHttpCodecs}
import msocket.impl.sse.QueryHeader
import msocket.impl.ws.WsServerFlow

class RoutesFactory[Req: Decoder](
    postHandler: MessageHandler[Req, Route],
    websocketHandler: Encoding[_] => MessageHandler[Req, Source[Message, NotUsed]],
    sseHandler: MessageHandler[Req, Route]
)(implicit mat: Materializer)
    extends ServerHttpCodecs {

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
        PostDirectives.withAcceptHeader {
          entity(as[Req])(postHandler.handle)
        }
      }
    }
  }

  private def extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => Json.decode(query.getBytes()).to[Req].value
  }
}
