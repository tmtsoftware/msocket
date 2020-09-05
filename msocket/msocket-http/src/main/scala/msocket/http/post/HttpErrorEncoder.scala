package msocket.http.post

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import io.bullet.borer.Encoder
import msocket.api.models.{ErrorType, ResponseHeaders}
import msocket.api.ErrorProtocol
import msocket.http.post.headers.ErrorTypeHeader
import msocket.jvm.ResponseEncoder

class HttpErrorEncoder[Req: ErrorProtocol] extends ResponseEncoder[Req, Route] with ServerHttpCodecs {
  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Route = {
    val errorType = headers.errorType.getOrElse(ErrorType.GenericError)

    val statusCode = errorType match {
      case ErrorType.AuthenticationError => StatusCodes.Unauthorized
      case ErrorType.AuthorizationError  => StatusCodes.Forbidden
      case _                             => StatusCodes.InternalServerError
    }

    complete(statusCode, Seq(ErrorTypeHeader(errorType.toString)), response)
  }
}
