package msocket.impl.ws

import akka.http.scaladsl.model.ws.Message
import io.bullet.borer.Encoder
import msocket.api.{ContentType, ErrorProtocol, MessageEncoder}
import msocket.impl.ws.WebsocketExtensions.WebsocketEncoding

class WebsocketMessageEncoder[Req: ErrorProtocol](contentType: ContentType) extends MessageEncoder[Req, Message] {
  override def encode[Res: Encoder](response: Res): Message = contentType.strictMessage[Res](response)
}
