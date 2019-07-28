package msocket.core.api

import io.bullet.borer.{Decoder, Encoder, Writer}
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveEncoderForUnaryCaseClass
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodecForUnaryCaseClass

case class Response[T: Encoder](value: T) {
  lazy val responseEncoder: Encoder[Response[T]] = deriveEncoderForUnaryCaseClass[Response[T]]
}

object Response {
  implicit def enc[T]: Encoder[Response[T]]                   = (w: Writer, value: Response[T]) => value.responseEncoder.write(w, value)
  implicit def dec[T: Decoder: Encoder]: Decoder[Response[T]] = deriveCodecForUnaryCaseClass[Response[T]].decoder
}
