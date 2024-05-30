package msocket.http

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.marshalling.Marshal
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import io.bullet.borer.Encoder
import msocket.api.models.{ErrorType, ServiceError}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.http.post.ClientHttpCodecs
import msocket.http.post.headers.{AppNameHeader, ErrorTypeHeader, UserNameHeader}

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class HttpUtils[Req: Encoder](
    val clientContentType: ContentType,
    uri: String,
    tokenFactory: () => Option[String],
    appName: Option[String] = None,
    username: Option[String] = None
)(implicit
    actorSystem: ActorSystem[?],
    ep: ErrorProtocol[Req]
) extends ClientHttpCodecs {

  import actorSystem.executionContext

  def getResponse(request: Req): Future[HttpResponse] = {
    val authHeader     = tokenFactory().map(t => Authorization(OAuth2BearerToken(t)))
    val appNameHeader  = appName.map(name => AppNameHeader(name))
    val usernameHeader = username.map(name => UserNameHeader(name))
    Marshal(request).to[RequestEntity].flatMap { requestEntity =>
      val httpRequest = HttpRequest(
        HttpMethods.POST,
        uri = uri,
        entity = requestEntity,
        headers = authHeader.toList ++ appNameHeader ++ usernameHeader
      )
      handleRequest(httpRequest)
    }
  }

  def handleRequest(httpRequest: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(httpRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK                  => Future.successful(response)
        case StatusCodes.InternalServerError =>
          val maybeErrorType = response.headers.collectFirst { case ErrorTypeHeader(value) =>
            ErrorType.from(value)
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
