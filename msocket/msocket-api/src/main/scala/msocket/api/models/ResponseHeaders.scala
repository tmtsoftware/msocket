package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

case class ResponseHeaders(errorType: Option[ErrorType] = None)

object ResponseHeaders {
  implicit lazy val headersCodec: Codec[ResponseHeaders] = MapBasedCodecs.deriveCodec

  def withErrorType(errorType: ErrorType): ResponseHeaders = ResponseHeaders(errorType = Some(errorType))
}
