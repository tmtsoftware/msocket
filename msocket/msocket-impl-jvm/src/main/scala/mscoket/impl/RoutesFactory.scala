package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.RequestHandler
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

class RoutesFactory[PostReq: Decoder, WebsocketReq: Encoder: Decoder](
    httpHandler: RequestHandler[PostReq, StandardRoute],
    websocketHandler: RequestHandler[WebsocketReq, Source[Message, NotUsed]]
) extends HttpCodecs {

  val route: Route = cors() {
    get {
      path("websocket") {
        handleWebSocketMessages {
          new WsServerFlow(websocketHandler).flow
        }
      }
    } ~
    post {
      path("post") {
        entity(as[PostReq])(httpHandler.handle)
      }
    }
  }
}
