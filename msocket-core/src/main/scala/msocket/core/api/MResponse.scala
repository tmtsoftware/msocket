package msocket.core.api

import io.bullet.borer.{Decoder, Encoder, Writer}
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveEncoderForUnaryCaseClass
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodecForUnaryCaseClass

case class MResponse[T: Encoder](value: T) {
  lazy val responseEncoder: Encoder[MResponse[T]] = deriveEncoderForUnaryCaseClass[MResponse[T]]
}

object MResponse {
  implicit def enc[T]: Encoder[MResponse[T]]                   = (w: Writer, value: MResponse[T]) => value.responseEncoder.write(w, value)
  implicit def dec[T: Decoder: Encoder]: Decoder[MResponse[T]] = deriveCodecForUnaryCaseClass[MResponse[T]].decoder
}
