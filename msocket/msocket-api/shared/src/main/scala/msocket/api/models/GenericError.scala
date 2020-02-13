package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.CompactMapBasedCodecs

case class GenericError(errorName: String, message: String) {
  override def toString: String =
    s"""
       |ErrorName : $errorName: 
       |Message   : $message
       |""".stripMargin
}

object GenericError {
  implicit lazy val genericErrorCodec: Codec[GenericError] = CompactMapBasedCodecs.deriveCodec
}
