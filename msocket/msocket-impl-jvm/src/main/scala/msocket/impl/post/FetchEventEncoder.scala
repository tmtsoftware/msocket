package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.Encoding.JsonText
import msocket.api.ErrorProtocol
import msocket.api.models.FetchEvent
import msocket.impl.MessageEncoder

class FetchEventEncoder[Req: ErrorProtocol] extends MessageEncoder[Req, FetchEvent] {
  override def encode[Res: Encoder](response: Res): FetchEvent = FetchEvent(JsonText.encode(response))
}
