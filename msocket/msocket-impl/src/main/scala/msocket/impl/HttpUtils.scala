package msocket.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import msocket.api.models.ServiceError
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.post.ClientHttpCodecs

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.util.control.NonFatal

class HttpUtils[Req](val clientContentType: ContentType)(implicit actorSystem: ActorSystem[_], ep: ErrorProtocol[Req])
    extends ClientHttpCodecs {

  import actorSystem.executionContext

  def handleRequest(httpRequest: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(httpRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK                  => Future.successful(response)
        case StatusCodes.InternalServerError => handleServiceError(response).map(throw _)
        case _                               => handleTransportError(response).map(throw _)
      }
    }
  }

  private def handleServiceError(response: HttpResponse): Future[Throwable] = {
    response.entity.toStrict(1.seconds).flatMap { x =>
      Unmarshal(x).to[ep.E].recoverWith {
        case NonFatal(_) => Unmarshal(x).to[ServiceError]
      }
    }
  }

  private def handleTransportError(response: HttpResponse): Future[Throwable] = {
    response.entity.toStrict(1.seconds).map { entity =>
      HttpError(response.status.intValue(), response.status.reason(), entity.data.utf8String)
    }
  }
}
