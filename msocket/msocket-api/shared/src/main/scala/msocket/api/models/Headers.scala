package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

sealed trait ErrorType {
  lazy val name: String = this.toString
}

object ErrorType {
  case object DomainError  extends ErrorType
  case object GenericError extends ErrorType

  def from(name: String): ErrorType = {
    name match {
      case DomainError.name  => DomainError
      case GenericError.name => GenericError
      case _                 => throw new RuntimeException(s"unsupported ErrorType: $name")
    }
  }

  implicit lazy val responseStatusCodec: Codec[ErrorType] = Codec.of[String].bimap[ErrorType](_.name, ErrorType.from)
}

case class Headers(accessToken: Option[String] = None, errorType: Option[ErrorType] = None)

object Headers {
  implicit lazy val headersCodec: Codec[Headers] = MapBasedCodecs.deriveCodec

  def withErrorType(errorType: ErrorType): Headers = Headers(errorType = Some(errorType))
}
