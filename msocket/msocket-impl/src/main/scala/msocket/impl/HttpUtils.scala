package msocket.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import msocket.api.models.{ErrorType, ServiceError}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.post.ClientHttpCodecs
import msocket.impl.post.headers.ErrorTypeHeader

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class HttpUtils[Req](val clientContentType: ContentType)(implicit actorSystem: ActorSystem[_], ep: ErrorProtocol[Req])
    extends ClientHttpCodecs {

  import actorSystem.executionContext

  def handleRequest(httpRequest: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(httpRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK                  => Future.successful(response)
        case StatusCodes.InternalServerError =>
          val maybeErrorType = response.headers.collectFirst {
            case ErrorTypeHeader(value) => ErrorType.from(value)
          }
          val entityF        = response.entity.toStrict(1.seconds)
          val errorF         = maybeErrorType match {
            case Some(ErrorType.DomainError) => entityF.flatMap(x => Unmarshal(x).to[ep.E])
            case Some(_)                     => entityF.flatMap(x => Unmarshal(x).to[ServiceError])
            case None                        => transportError(entityF, response.status)
          }
          errorF.map(throw _)
        case _                               => transportError(response.entity.toStrict(1.seconds), response.status).map(throw _)
      }
    }
  }

  private def transportError(entityF: Future[HttpEntity.Strict], status: StatusCode): Future[Throwable] = {
    entityF.map(entity => HttpError(status.intValue(), status.reason(), entity.data.utf8String))
  }
}
