package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

case class ProtocolError(errorName: String, message: String) {
  override def toString: String = s"$errorName: $message"
}

object ProtocolError {
  implicit lazy val protocolErrorCodec: Codec[ProtocolError] = deriveCodec
}
