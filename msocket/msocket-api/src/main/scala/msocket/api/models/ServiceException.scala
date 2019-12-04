package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs

case class ServiceException(protocol_error: ProtocolError) extends RuntimeException(protocol_error.toString)

object ServiceException {
  implicit lazy val serviceExceptionCodec: Codec[ServiceException] = ArrayBasedCodecs.deriveUnaryCodec

  def fromThrowable(ex: Throwable): ServiceException = ServiceException(ProtocolError(ex.getClass.getSimpleName, ex.getMessage))
}
