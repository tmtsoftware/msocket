package msocket.impl.rsocket

import msocket.api.{ContentEncoding, ContentType}
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType.{Cbor, Json}
import typings.node.Buffer
import typings.rsocketDashCore.rSocketEncodingMod.Encoders
import typings.rsocketDashCore.rsocketDashCoreMod

trait RSocketEncoders[CT <: ContentType] {
  type En
  def contentEncoding: ContentEncoding[En]
  def encoders: Encoders[En]
}

object RSocketEncoders {
  case class RSocketEncodersFactory[CT <: ContentType, _En](contentEncoding: ContentEncoding[_En], encoders: Encoders[_En])
      extends RSocketEncoders[CT] {
    override type En = _En
  }

  implicit object JsonRSocketEncoders   extends RSocketEncodersFactory[Json.type, String](JsonText, rsocketDashCoreMod.Utf8Encoders)
  implicit object BufferRSocketEncoders extends RSocketEncodersFactory[Cbor.type, Buffer](CborNodeBuffer, rsocketDashCoreMod.BufferEncoders)
}
