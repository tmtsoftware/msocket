package msocket.impl.rsocket

import io.bullet.borer.{Cbor, Json, Target}
import msocket.api.Encoding
import msocket.api.Encoding.JsonText
import typings.node.Buffer
import typings.rsocketDashCore.rSocketEncodingMod.Encoders
import typings.rsocketDashCore.rsocketDashCoreMod

trait RSocketEncoders[CT <: Target] {
  type En
  def encoding: Encoding[En]
  def encoders: Encoders[En]
}

object RSocketEncoders {
  case class RSocketEncodersFactory[CT <: Target, _En](encoding: Encoding[_En], encoders: Encoders[_En]) extends RSocketEncoders[CT] {
    override type En = _En
  }

  implicit object JsonRSocketEncoders   extends RSocketEncodersFactory[Json.type, String](JsonText, rsocketDashCoreMod.Utf8Encoders)
  implicit object BufferRSocketEncoders extends RSocketEncodersFactory[Cbor.type, Buffer](CborNodeBuffer, rsocketDashCoreMod.BufferEncoders)
}
