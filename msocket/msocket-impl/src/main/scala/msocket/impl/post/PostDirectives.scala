package msocket.impl.post

import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpRequest, MediaRanges}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, ExceptionHandler, Route}
import io.bullet.borer.Decoder
import io.prometheus.client.Counter
import msocket.api.{ErrorProtocol, Labellable}
import msocket.impl.post.ServerHttpCodecs._

object PostDirectives {

  def addMissingAcceptHeader(request: HttpRequest): HttpRequest = {
    val mediaType = request.entity.contentType.mediaType
    request.header[Accept] match {
      case None | Some(Accept(Seq(MediaRanges.`*/*`))) => request.removeHeader(Accept.name).addHeader(Accept(mediaType))
      case Some(_)                                     => request
    }
  }

  def withMetrics[Req: Decoder: ErrorProtocol](counter: Counter)(handle: Req => Route)(implicit labels: Req => Labellable[Req]): Route =
    extractRequest { request =>
      val hostAddress = request.uri.authority.host.address

      entity(as[Req]) { req =>
        val labelValues = labels(req).metricLabels().labels
        val msgValue    = labelValues("msg")
        val updated     = labelValues.removed("msg").values

        counter.labels(List(msgValue, hostAddress) ++ updated: _*)
        handle(req)
      }
    }

  val withAcceptHeader: Directive0 = mapRequest(addMissingAcceptHeader)

  def exceptionHandlerFor[Req: ErrorProtocol]: Directive0 = handleExceptions {
    ExceptionHandler(new HttpErrorEncoder[Req].errorEncoder)
  }

}
