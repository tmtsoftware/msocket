package msocket.api

import io.bullet.borer.Encoder
import msocket.api.models.{ResponseHeaders, ErrorType, ServiceError}

import scala.util.control.NonFatal

abstract class ResponseEncoder[Req, M](implicit ep: ErrorProtocol[Req]) {
  def encode[Res: Encoder](response: Res, headers: ResponseHeaders): M

  lazy val errorEncoder: PartialFunction[Throwable, M] = {
    case NonFatal(ex: ep.E) => encode(ex, ResponseHeaders.withErrorType(ErrorType.DomainError))
    case NonFatal(ex)       => encode(ServiceError.fromThrowable(ex), ResponseHeaders.withErrorType(ErrorType.GenericError))
  }
}
