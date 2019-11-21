package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

case class StreamError(name: String, message: String)

object StreamError {
  implicit lazy val streamErrorCodec: Codec[StreamError] = deriveCodec
}
