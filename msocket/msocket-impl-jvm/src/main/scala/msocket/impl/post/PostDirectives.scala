package msocket.impl.post

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.{Directive0, Directives}

object PostDirectives {

  def addMissingAcceptHeader(request: HttpRequest): HttpRequest = {
    request.header[Accept] match {
      case Some(_) => request
      case None    => request.addHeader(Accept(request.entity.contentType.mediaType))
    }
  }

  val withAcceptHeader: Directive0 = Directives.mapRequest(addMissingAcceptHeader)
}
