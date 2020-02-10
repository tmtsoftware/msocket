package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ErrorProtocol, MessageEncoder}

class FetchEventEncoder[Req: ErrorProtocol] extends MessageEncoder[Req, FetchEvent] {
  override def encode[Res: Encoder](response: Res): FetchEvent = FetchEvent(JsonText.encode(response))
}
