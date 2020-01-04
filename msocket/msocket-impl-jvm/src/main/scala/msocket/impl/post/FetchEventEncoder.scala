package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.Encoding.JsonText
import msocket.api.{ErrorProtocol, MessageEncoder}
import msocket.api.models.FetchEvent

class FetchEventEncoder[Req: ErrorProtocol] extends MessageEncoder[Req, FetchEvent] {
  override def encode[Res: Encoder](response: Res): FetchEvent = FetchEvent(JsonText.encode(response))
}
