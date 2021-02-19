package msocket.js.rsocket

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding
import msocket.api.ContentEncoding.CborByteArray
import msocket.api.ContentType.Cbor
import tmttyped.node
import tmttyped.node.bufferMod.Buffer

import scala.scalajs.js
import scala.scalajs.js.typedarray.{AB2TA, Int8Array}

case object CborNodeBuffer extends ContentEncoding[node.Buffer](Cbor) {
  def encode[T: Encoder](payload: T): node.Buffer =
    Buffer.from(CborByteArray.encode(payload).toTypedArray.asInstanceOf[js.typedarray.Uint8Array])

  def decode[T: Decoder](input: node.Buffer): T =
    CborByteArray.decode(input.asInstanceOf[Int8Array].toArray)
}
