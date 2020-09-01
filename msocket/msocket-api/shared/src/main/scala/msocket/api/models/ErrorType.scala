package msocket.api.models

import io.bullet.borer.Codec

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