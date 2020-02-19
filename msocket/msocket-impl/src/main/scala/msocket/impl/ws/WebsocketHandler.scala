package msocket.impl.ws

import akka.http.scaladsl.model.ws.Message
import io.bullet.borer.Encoder
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.StreamHandler
import msocket.impl.ws.WebsocketExtensions.WebsocketEncoding

/**
 * This helper class can be extended to define custom  websocket handler in the server which returns [[Source]] of [[Message]].
 * WebsocketHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class WebsocketHandler[Req: ErrorProtocol](contentType: ContentType) extends StreamHandler[Req, Message] {
  override def encode[Res: Encoder](response: Res): Message = contentType.strictMessage[Res](response)
}
