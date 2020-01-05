package msocket.impl.ws

import akka.http.scaladsl.model.ws.Message
import io.bullet.borer.Encoder
import msocket.api.{Encoding, ErrorProtocol, MessageEncoder}
import msocket.impl.ws.WebsocketExtensions.WebsocketEncoding

class WebsocketMessageEncoder[Req: ErrorProtocol](encoding: Encoding[_]) extends MessageEncoder[Req, Message] {
  override def encode[Res: Encoder](response: Res): Message = encoding.strictMessage[Res](response)
}
