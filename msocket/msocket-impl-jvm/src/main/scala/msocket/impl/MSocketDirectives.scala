package msocket.impl

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, ExceptionHandler}
import msocket.api.models.MSocketException
import msocket.impl.post.ServerHttpCodecs

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

  val withExceptionHandler: Directive0 = handleExceptions {
    ExceptionHandler {
      case NonFatal(ex) => complete(MSocketException.fromThrowable(ex))
    }
  }

}
