package msocket.impl

import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, ExceptionHandler}
import io.bullet.borer.Encoder
import msocket.api.models.ServiceException
import msocket.impl.post.ServerHttpCodecs

import scala.reflect.ClassTag
import scala.util.control.NonFatal

object MSocketDirectives {
  import ServerHttpCodecs._

  def addMissingAcceptHeader(request: HttpRequest): HttpRequest = {
    request.header[Accept] match {
      case Some(_) => request
      case None    => request.addHeader(Accept(request.entity.contentType.mediaType))
    }
  }

  val withAcceptHeader: Directive0 = mapRequest(addMissingAcceptHeader)

  def withExceptionHandler[Err <: Throwable: Encoder: ClassTag]: Directive0 = handleExceptions {
    ExceptionHandler {
      case NonFatal(ex: Err) => complete(StatusCodes.InternalServerError -> ex)
      case NonFatal(ex)      => complete(StatusCodes.InternalServerError -> ServiceException.fromThrowable(ex))
    }
  }

}
