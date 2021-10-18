package msocket.http.post

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.api.models.{ErrorType, ResponseHeaders}
import msocket.http.post.headers.ErrorTypeHeader
import msocket.jvm.mono.MonoResponseEncoder
import msocket.security.AccessController

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext

class HttpResponseEncoder[Req: ErrorProtocol](val accessController: AccessController)(implicit ec: ExecutionContext)
    extends MonoResponseEncoder[Req, Route]
    with ServerHttpCodecs {

  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Route = {
    val statusCode = headers.errorType match {
      case Some(value) =>
        value match {
          case ErrorType.TokenMissingError   => StatusCodes.Unauthorized
          case ErrorType.AuthenticationError => StatusCodes.Unauthorized
          case ErrorType.AuthorizationError  => StatusCodes.Forbidden
          case _                             => StatusCodes.InternalServerError
        }
      case None        =>
        StatusCodes.OK
    }

    val extraHeaders = headers.errorType match {
      case Some(value) => Seq(ErrorTypeHeader(value.toString))
      case None        => Seq.empty
    }

    complete(statusCode, extraHeaders, response)
  }
}
