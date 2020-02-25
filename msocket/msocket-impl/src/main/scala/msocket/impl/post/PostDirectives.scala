package msocket.impl.post

import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpRequest, MediaRanges}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, ExceptionHandler, Route}
import io.bullet.borer.Decoder
import io.prometheus.client.Counter
import msocket.api.{ErrorProtocol, Labelled}
import msocket.impl.post.ServerHttpCodecs._

object PostDirectives {

  def addMissingAcceptHeader(request: HttpRequest): HttpRequest = {
    val mediaType = request.entity.contentType.mediaType
    request.header[Accept] match {
      case None | Some(Accept(Seq(MediaRanges.`*/*`))) => request.removeHeader(Accept.name).addHeader(Accept(mediaType))
      case Some(_)                                     => request
    }
  }

  def withMetrics[Req: Decoder: ErrorProtocol](counter: Counter)(handle: Req => Route)(implicit labelGen: Req => Labelled[Req]): Route =
    extractRequest { httpRequest =>
      val hostAddress = httpRequest.uri.authority.host.address

      entity(as[Req]) { req =>
        val labels = labelGen(req).labels().withHost(hostAddress).labelValues

        counter.labels(labels: _*).inc()
        handle(req)
      }
    }

  val withAcceptHeader: Directive0 = mapRequest(addMissingAcceptHeader)

  def exceptionHandlerFor[Req: ErrorProtocol]: Directive0 = handleExceptions {
    ExceptionHandler(new HttpErrorEncoder[Req].errorEncoder)
  }

}
