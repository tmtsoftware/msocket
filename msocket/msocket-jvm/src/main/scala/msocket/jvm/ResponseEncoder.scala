package msocket.jvm

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.api.models.ErrorType.{AuthenticationError, AuthorizationError, DomainError, GenericError, TokenMissingError}
import msocket.api.models.{ResponseHeaders, ServiceError}
import msocket.security.models.AccessStatus.{AuthenticationFailed, AuthorizationFailed, TokenMissing}

import scala.util.control.NonFatal

abstract class ResponseEncoder[Req, M](implicit ep: ErrorProtocol[Req]) {
  def encode[Res: Encoder](response: Res, headers: ResponseHeaders): M

  lazy val errorEncoder: PartialFunction[Throwable, M] = {
    case NonFatal(ex: ep.E)                 => encode(ex, ResponseHeaders.withErrorType(DomainError))
    case NonFatal(ex: TokenMissing)         => encode(ServiceError.fromThrowable(ex), ResponseHeaders.withErrorType(TokenMissingError))
    case NonFatal(ex: AuthenticationFailed) => encode(ServiceError.fromThrowable(ex), ResponseHeaders.withErrorType(AuthenticationError))
    case NonFatal(ex: AuthorizationFailed)  => encode(ServiceError.fromThrowable(ex), ResponseHeaders.withErrorType(AuthorizationError))
    case NonFatal(ex)                       => encode(ServiceError.fromThrowable(ex), ResponseHeaders.withErrorType(GenericError))
  }
}
