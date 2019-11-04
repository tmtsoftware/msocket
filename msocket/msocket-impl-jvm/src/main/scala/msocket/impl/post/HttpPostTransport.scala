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
import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.Transport
import msocket.api.models._
import msocket.impl.Encoding
import msocket.impl.StreamSplitter._

import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class HttpPostTransport[Req: Encoder](uri: String, messageEncoding: Encoding[_], tokenFactory: () => Option[String])(
    implicit actorSystem: ActorSystem[_]
) extends Transport[Req]
    with ClientHttpCodecs {

  override def encoding: Encoding[_] = messageEncoding

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    getResponse(request).flatMap(Unmarshal(_).to[Res])
  }

  override def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[FetchEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .filter(_ != FetchEvent.Heartbeat)
      .map(event => Json.decode(event.data.getBytes()).to[Res].value)
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }

  override def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    requestStream[Result[Res, StreamError]](request).split
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
          case statusCode =>
            response.entity.toStrict(1.seconds).map(x => throw HttpException(statusCode.intValue(), statusCode.reason(), x.toString()))
        }
      }
    }
  }

}
