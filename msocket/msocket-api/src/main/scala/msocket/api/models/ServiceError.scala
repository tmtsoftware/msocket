package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.CompactMapBasedCodecs

case class ServiceError(generic_error: GenericError) extends RuntimeException(generic_error.toString)

object ServiceError {
  implicit lazy val serviceErrorCodec: Codec[ServiceError] = CompactMapBasedCodecs.deriveCodec

  def fromThrowable(ex: Throwable): ServiceError = ServiceError(GenericError(ex.getClass.getSimpleName, ex.getMessage))
}
