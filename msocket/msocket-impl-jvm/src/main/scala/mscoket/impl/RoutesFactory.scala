package mscoket.impl

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{PostHandler, WebsocketHandler}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

class RoutesFactory[PostReq: Decoder, WebsocketReq: Encoder: Decoder](
    httpHandler: PostHandler[PostReq, StandardRoute],
    websocketHandler: WebsocketHandler[WebsocketReq]
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
