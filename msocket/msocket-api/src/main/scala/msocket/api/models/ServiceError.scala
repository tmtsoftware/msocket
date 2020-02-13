package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.CompactMapBasedCodecs

/**
 * There might be cases when the request encounters runtime error during executions.
 * In such cases Service error is returned since no domain specific error can be mapped to such exceptions.
 * Service error is returned as response in these scenarios.
 */
case class ServiceError(generic_error: GenericError) extends RuntimeException(generic_error.toString)

object ServiceError {
  implicit lazy val serviceErrorCodec: Codec[ServiceError] = CompactMapBasedCodecs.deriveCodec

  def fromThrowable(ex: Throwable): ServiceError = ServiceError(GenericError(ex.getClass.getSimpleName, ex.getMessage))
}
