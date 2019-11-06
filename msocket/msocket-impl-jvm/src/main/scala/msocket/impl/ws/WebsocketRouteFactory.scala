package msocket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.bullet.borer.Decoder
import msocket.api.MessageHandler
import msocket.impl.{Encoding, RouteFactory}
import msocket.impl.post.ServerHttpCodecs

class WebsocketRouteFactory[Req: Decoder](endpoint: String, websocketHandler: Encoding[_] => MessageHandler[Req, Source[Message, NotUsed]])(
    implicit mat: Materializer
) extends RouteFactory
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
