package msocket.impl.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.{ContentType, ErrorProtocol, MessageEncoder}
import msocket.impl.rsocket.RSocketExtensions._

class RSocketPayloadEncoder[Req: ErrorProtocol](contentType: ContentType) extends MessageEncoder[Req, Payload] {
  override def encode[Res: Encoder](response: Res): Payload = contentType.payload(response)
}
