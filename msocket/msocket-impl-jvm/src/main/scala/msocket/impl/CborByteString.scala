package msocket.impl

import akka.util.ByteString
import io.bullet.borer.{Cbor, Decoder, Encoder}
import io.bullet.borer.compat.akka._
import msocket.api.Encoding

case object CborByteString extends Encoding[ByteString] {
  def encode[T: Encoder](payload: T): ByteString = Cbor.encode(payload).to[ByteString].result
  def decode[T: Decoder](input: ByteString): T   = Cbor.decode(input).to[T].value
}
