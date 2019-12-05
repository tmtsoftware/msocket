package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.post.ServerHttpCodecs
import msocket.impl.{Encoding, RouteFactory}

class WebsocketRouteFactory[Req: Decoder: ErrorProtocol](
    endpoint: String,
    websocketHandler: Encoding[_] => MessageHandler[Req, Source[Message, NotUsed]]
)(implicit actorSystem: ActorSystem[_])
    extends RouteFactory
    with ServerHttpCodecs {

  def make(): Route = {
    get {
      path(endpoint) {
        handleWebSocketMessages {
          new WsServerFlow(websocketHandler).flow
        }
      }
    }
  }
}
