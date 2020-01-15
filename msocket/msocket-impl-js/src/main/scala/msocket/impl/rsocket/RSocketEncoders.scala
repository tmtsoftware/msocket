package msocket.impl.rsocket

import msocket.api.Encoding
import msocket.api.Encoding.JsonText
import typings.node.Buffer
import typings.rsocketDashCore.rSocketEncodingMod.Encoders
import typings.rsocketDashCore.rsocketDashCoreMod

case class RSocketEncoders[En](encoding: Encoding[En], encoders: Encoders[En])

object RSocketEncoders {
  implicit object JsonRSocketEncoders   extends RSocketEncoders[String](JsonText, rsocketDashCoreMod.Utf8Encoders)
  implicit object BufferRSocketEncoders extends RSocketEncoders[Buffer](CborNodeBuffer, rsocketDashCoreMod.BufferEncoders)
}
