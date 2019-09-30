package mscoket.impl.post

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder, Json}
import mscoket.impl.HttpCodecs
import mscoket.impl.StreamSplitter._
import msocket.api.Transport
import msocket.api.utils.{FetchEvent, HttpException, Result, StreamStatus}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class HttpPostTransport[Req: Encoder](uri: String, tokenFactory: => Option[String])(implicit actorSystem: ActorSystem)
    extends Transport[Req]
    with HttpCodecs {

  implicit lazy val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext   = actorSystem.dispatcher

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    getResponse(request).flatMap(Unmarshal(_).to[Res])
  }

  override def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[FetchEvent, NotUsed]])
    Source
      .fromFutureSource(futureSource)
      .filter(_ != FetchEvent.Heartbeat)
      .map(event => Json.decode(event.data.getBytes()).to[Res].value)
      .mapMaterializedValue(_ => NotUsed)
  }

  override def requestStreamWithError[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    requestStream[Result[Res, StreamStatus]](request).split
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    Marshal(request).to[RequestEntity].flatMap { requestEntity =>
      val httpRequest = HttpRequest(
        HttpMethods.POST,
        uri = uri,
        entity = requestEntity,
        headers = tokenFactory match {
          case Some(token) => Seq(Authorization(OAuth2BearerToken(token)))
          case None        => Nil
        }
      )
      Http().singleRequest(httpRequest).flatMap { response =>
        response.status match {
          case StatusCodes.OK => Future.successful(response)
          case statusCode =>
            response.entity.toStrict(1.seconds).map(x => throw HttpException(statusCode.intValue(), statusCode.reason(), x.toString()))
        }
      }
    }
  }

}
