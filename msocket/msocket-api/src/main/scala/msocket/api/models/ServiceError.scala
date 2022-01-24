package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.CompactMapBasedCodecs

/**
 * There might be cases when the request encounters runtime error during executions.
 * In such cases Service error is returned since no domain specific error can be mapped to such exceptions.
 * Service error is returned as response in these scenarios.
 * _ in field names is intentional to make it harder for this to get deserialized as a domain model with same fields
 */
case class ServiceError(error_name: String, error_message: String) extends RuntimeException(s"$error_name: $error_message")

object ServiceError {
  implicit lazy val serviceErrorCodec: Codec[ServiceError] = CompactMapBasedCodecs.deriveCodec

  def fromThrowable(ex: Throwable): ServiceError = ServiceError(ex.getClass.getSimpleName, ex.getMessage)
}
