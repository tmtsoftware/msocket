package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

case class MSocketException(protocol_error: ProtocolError) extends RuntimeException(protocol_error.toString)

object MSocketException {
  implicit lazy val MSocketExceptionCodec: Codec[MSocketException] = deriveCodec

  def fromThrowable(ex: Throwable): MSocketException = MSocketException(ProtocolError(ex.getClass.getSimpleName, ex.getMessage))
}
