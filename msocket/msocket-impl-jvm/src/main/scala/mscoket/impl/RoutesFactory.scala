package mscoket.impl

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{PostHandler, WebsocketHandler}

class RoutesFactory[PostReq: Decoder, WebsocketReq: Encoder: Decoder](
    httpHandler: PostHandler[PostReq, StandardRoute],
    websocketHandler: WebsocketHandler[WebsocketReq]
) extends HttpCodecs {

  val route: Route = get {
    path("websocket" / Segment) { encoding =>
      handleWebSocketMessages {
        new WsServerFlow(websocketHandler).flow(Encoding.fromString(encoding))
      }
    }
  } ~
    post {
      path("post") {
        entity(as[PostReq])(httpHandler.handle)
      }
    }
}
