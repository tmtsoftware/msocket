package msocket.impl

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding
import msocket.api.Encoding.CborByteArray
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer
import typings.node.bufferMod.Buffer
import typings.{node, std}

import scala.scalajs.js.typedarray._

case object CborArrayBuffer extends Encoding[ArrayBuffer](Encoding.ApplicationCbor) {
  def encode[T: Encoder](payload: T): ArrayBuffer = CborByteArray.encode(payload).toTypedArray.buffer
  def decode[T: Decoder](input: ArrayBuffer): T   = CborByteArray.decode(TypedArrayBuffer.wrap(input).toByteArray)
}

case object CborNodeBuffer extends Encoding[node.Buffer](Encoding.ApplicationCbor) {
  def encode[T: Encoder](payload: T): node.Buffer = Buffer.from(CborByteArray.encode(payload).toTypedArray.asInstanceOf[std.Uint8Array])
  def decode[T: Decoder](input: node.Buffer): T   = CborByteArray.decode(input.asInstanceOf[Int8Array].toArray)
}
