package msocket.api.utils

import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ErrorProtocol
import msocket.api.models.ErrorType.{DomainError, GenericError}
import msocket.api.models.{ResponseHeaders, ServiceError}

import scala.util.control.NonFatal

class LoggingResponseEncoder[Req](action: String => Unit = println)(implicit ep: ErrorProtocol[Req]) {
  def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Unit = {
    action(s"Response <-- ${JsonText.encode(response)}")
  }

  lazy val errorEncoder: PartialFunction[Throwable, Unit] = {
    case NonFatal(ex: ep.E) => encode(ex, ResponseHeaders.withErrorType(DomainError))
    case NonFatal(ex)       => encode(ServiceError.fromThrowable(ex), ResponseHeaders.withErrorType(GenericError))
  }
}
