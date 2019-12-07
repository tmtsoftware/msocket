package msocket.impl

import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpRequest, MediaRanges, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, ExceptionHandler}
import msocket.api.ErrorProtocol
import msocket.api.models.ServiceError
import msocket.impl.post.ServerHttpCodecs

import scala.util.control.NonFatal

object MSocketDirectives {
  import ServerHttpCodecs._

  def addMissingAcceptHeader(request: HttpRequest): HttpRequest = {
    val mediaType = request.entity.contentType.mediaType
    request.header[Accept] match {
      case None | Some(Accept(Seq(MediaRanges.`*/*`))) => request.removeHeader(Accept.name).addHeader(Accept(mediaType))
      case Some(_)                                     => request
    }
  }

  val withAcceptHeader: Directive0 = mapRequest(addMissingAcceptHeader)

  def withExceptionHandler[Req](implicit ep: ErrorProtocol[Req]): Directive0 = handleExceptions {
    ExceptionHandler {
      case NonFatal(ex: ep.E) => complete(StatusCodes.InternalServerError -> ex)
      case NonFatal(ex)       => complete(StatusCodes.InternalServerError -> ServiceError.fromThrowable(ex))
    }
  }

}
