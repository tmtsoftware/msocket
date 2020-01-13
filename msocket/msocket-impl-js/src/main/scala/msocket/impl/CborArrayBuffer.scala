package msocket.impl

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding
import msocket.api.Encoding.{CborBinary, CborByteBuffer}
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer

import scala.scalajs.js.typedarray.{ArrayBuffer, _}

case object CborArrayBuffer extends Encoding[ArrayBuffer](CborByteBuffer.mimeType) {
  private case object CborByteArray extends CborBinary[Array[Byte]]

  def encode[T: Encoder](payload: T): ArrayBuffer = CborByteArray.encode(payload).toTypedArray.buffer
  def decode[T: Decoder](input: ArrayBuffer): T   = CborByteArray.decode(TypedArrayBuffer.wrap(input).toByteArray)
}
