package msocket.api.models

import io.bullet.borer.Codec

sealed trait ErrorType {
  lazy val name: String = this.toString
}

object ErrorType {
  case object DomainError         extends ErrorType
  case object GenericError        extends ErrorType
  case object TokenMissingError   extends ErrorType
  case object AuthenticationError extends ErrorType
  case object AuthorizationError  extends ErrorType

  def from(name: String): ErrorType = {
    name match {
      case DomainError.name         => DomainError
      case GenericError.name        => GenericError
      case TokenMissingError.name   => AuthenticationError
      case AuthenticationError.name => AuthenticationError
      case AuthorizationError.name  => AuthorizationError
      case _                        => throw new RuntimeException(s"unsupported ErrorType: $name")
    }
  }

  implicit lazy val responseStatusCodec: Codec[ErrorType] = Codec.of[String].bimap[ErrorType](_.name, ErrorType.from)
}
