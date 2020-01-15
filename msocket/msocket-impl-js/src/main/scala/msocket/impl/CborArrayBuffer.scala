package msocket.impl

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding
import msocket.api.Encoding.CborByteArray
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer

import scala.scalajs.js.typedarray._

case object CborArrayBuffer extends Encoding[ArrayBuffer](Encoding.ApplicationCbor) {
  def encode[T: Encoder](payload: T): ArrayBuffer = CborByteArray.encode(payload).toTypedArray.buffer
  def decode[T: Decoder](input: ArrayBuffer): T   = CborByteArray.decode(TypedArrayBuffer.wrap(input).toByteArray)
}
