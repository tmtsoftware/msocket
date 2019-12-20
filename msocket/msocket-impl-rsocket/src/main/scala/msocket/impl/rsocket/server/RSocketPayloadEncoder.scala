package msocket.impl.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.api.Encoding.CborByteBuffer
import msocket.api.ErrorProtocol
import msocket.impl.MessageEncoder

class RSocketPayloadEncoder[Req: ErrorProtocol] extends MessageEncoder[Req, Payload] {
  override def encode[Res: Encoder](response: Res): Payload = DefaultPayload.create(CborByteBuffer.encode(response))
}
