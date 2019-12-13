package msocket.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import msocket.api.{Encoding, ErrorProtocol}
import msocket.api.models.{HttpError, ServiceError}
import msocket.impl.post.ClientHttpCodecs

import scala.concurrent.Future
import scala.util.control.NonFatal
import concurrent.duration.DurationLong

class HttpUtils[Req](val encoding: Encoding[_])(implicit actorSystem: ActorSystem[_], ep: ErrorProtocol[Req]) extends ClientHttpCodecs {

  import actorSystem.executionContext

  def handleRequest(httpRequest: HttpRequest): Future[HttpResponse] = {
    Http(actorSystem).singleRequest(httpRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK => Future.successful(response)
        case _              => handleError(response).map(throw _)
      }
    }
  }

  private def handleError(response: HttpResponse): Future[Throwable] = {
    response.entity
      .toStrict(1.seconds)
      .flatMap { x =>
        Unmarshal(x).to[ep.E].recoverWith {
          case NonFatal(_) =>
            Unmarshal(x).to[ServiceError].recover {
              case NonFatal(_) => HttpError(response.status.intValue(), response.status.reason(), x.data.utf8String)
            }
        }
      }
  }
}
