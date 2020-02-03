package msocket.impl.rsocket

import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType.{Cbor, Json}
import msocket.api.{ContentEncoding, ContentType}
import typings.node.Buffer
import typings.rsocketCore.mod
import typings.rsocketCore.rsocketencodingMod.Encoders

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

  implicit object JsonRSocketEncoders   extends RSocketEncodersFactory[Json.type, String](JsonText, mod.Utf8Encoders)
  implicit object BufferRSocketEncoders extends RSocketEncodersFactory[Cbor.type, Buffer](CborNodeBuffer, mod.BufferEncoders)
}
