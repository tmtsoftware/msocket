package msocket.core.api

import io.bullet.borer.{Decoder, Encoder, Writer}
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveUnaryCodec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveUnaryEncoder

case class Payload[T: Encoder](value: T) {
  lazy val responseEncoder: Encoder[Payload[T]] = deriveUnaryEncoder[Payload[T]]
}

object Payload {
  implicit def enc[T]: Encoder[Payload[T]]                   = (w: Writer, value: Payload[T]) => value.responseEncoder.write(w, value)
  implicit def dec[T: Decoder: Encoder]: Decoder[Payload[T]] = deriveUnaryCodec[Payload[T]].decoder
}
