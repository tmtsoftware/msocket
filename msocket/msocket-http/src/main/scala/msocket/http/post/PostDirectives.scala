package msocket.http.post

import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpRequest, MediaRanges}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, ExceptionHandler}
import msocket.api.ErrorProtocol
import msocket.security.AccessControllerFactory

import scala.concurrent.ExecutionContext

object PostDirectives {

  def addMissingAcceptHeader(request: HttpRequest): HttpRequest = {
    val mediaType = request.entity.contentType.mediaType
    request.header[Accept] match {
      case None | Some(Accept(Seq(MediaRanges.`*/*`))) => request.removeHeader(Accept.name).addHeader(Accept(mediaType))
      case Some(_)                                     => request
    }
  }

  val withAcceptHeader: Directive0 = mapRequest(addMissingAcceptHeader)

  def exceptionHandlerFor[Req: ErrorProtocol](implicit ec: ExecutionContext): Directive0 =
    handleExceptions {
      ExceptionHandler(new HttpResponseEncoder[Req](AccessControllerFactory.noop.make(None)).errorEncoder)
    }

}
