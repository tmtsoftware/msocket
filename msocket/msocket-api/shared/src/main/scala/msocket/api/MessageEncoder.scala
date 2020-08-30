package msocket.api

import io.bullet.borer.Encoder
import msocket.api.models.{Headers, ErrorType, ServiceError}

import scala.util.control.NonFatal

abstract class MessageEncoder[Req, M](implicit ep: ErrorProtocol[Req]) {
  def encode[Res: Encoder](response: Res, headers: Headers): M

  lazy val errorEncoder: PartialFunction[Throwable, M] = {
    case NonFatal(ex: ep.E) => encode(ex, Headers.withErrorType(ErrorType.DomainError))
    case NonFatal(ex)       => encode(ServiceError.fromThrowable(ex), Headers.withErrorType(ErrorType.GenericError))
  }
}
