package msocket.impl.post

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models._
import msocket.api.{ErrorProtocol, Subscription, Transport}
import msocket.impl.Encoding
import msocket.impl.Encoding.JsonText

import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class HttpPostTransport[Req: Encoder](uri: String, messageEncoding: Encoding[_], tokenFactory: () => Option[String])(
    implicit actorSystem: ActorSystem[_],
    ep: ErrorProtocol[Req]
) extends Transport[Req]
    with ClientHttpCodecs {

  override def encoding: Encoding[_] = messageEncoding

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    getResponse(request).flatMap(Unmarshal(_).to[Res])
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream[Res](request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[FetchEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .filter(_ != FetchEvent.Heartbeat)
      .map(event => JsonText.decodeWithError[Res, Req](event.data))
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    Marshal(request).to[RequestEntity].flatMap { requestEntity =>
      val httpRequest = HttpRequest(
        HttpMethods.POST,
        uri = uri,
        entity = requestEntity,
        headers = tokenFactory() match {
          case Some(token) => Seq(Authorization(OAuth2BearerToken(token)))
          case None        => Nil
        }
      )
      Http()(actorSystem.toClassic).singleRequest(httpRequest).flatMap { response =>
        response.status match {
          case StatusCodes.OK => Future.successful(response)
          case _              => handleError(response).map(throw _)
        }
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
