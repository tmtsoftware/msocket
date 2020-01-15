package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding
import msocket.api.Encoding.CborByteArray
import typings.node.bufferMod.Buffer
import typings.{node, std}

import scala.scalajs.js.typedarray._

case object CborNodeBuffer extends Encoding[node.Buffer](Encoding.ApplicationCbor) {
  def encode[T: Encoder](payload: T): node.Buffer = Buffer.from(CborByteArray.encode(payload).toTypedArray.asInstanceOf[std.Uint8Array])
  def decode[T: Decoder](input: node.Buffer): T   = CborByteArray.decode(input.asInstanceOf[Int8Array].toArray)
}
