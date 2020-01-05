package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.RouteFactory
import msocket.impl.post.ServerHttpCodecs

class WebsocketRouteFactory[Req: Decoder: ErrorProtocol](endpoint: String, websocketHandler: Encoding[_] => WebsocketHandler[Req])(
    implicit actorSystem: ActorSystem[_]
) extends RouteFactory
    with ServerHttpCodecs {

  def make(): Route = {
    get {
      path(endpoint) {
        handleWebSocketMessages {
          new WebsocketServerFlow(websocketHandler).flow
        }
      }
    }
  }
}
