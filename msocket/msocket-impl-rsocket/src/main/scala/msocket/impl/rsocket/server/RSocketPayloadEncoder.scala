package msocket.impl.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.{Encoding, ErrorProtocol, MessageEncoder}
import msocket.impl.rsocket.RSocketExtensions._

class RSocketPayloadEncoder[Req: ErrorProtocol](encoding: Encoding[_]) extends MessageEncoder[Req, Payload] {
  override def encode[Res: Encoder](response: Res): Payload = encoding.payload(response)
}
