package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

case class Headers(
    accessToken: Option[String] = None,
    errorType: Option[ErrorType] = None,
    appName: Option[String] = None
)

object Headers {
  implicit lazy val headersCodec: Codec[Headers] = MapBasedCodecs.deriveCodec

  def withErrorType(errorType: ErrorType): Headers = Headers(errorType = Some(errorType))
}
