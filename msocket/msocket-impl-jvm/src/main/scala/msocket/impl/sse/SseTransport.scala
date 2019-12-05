package msocket.impl.sse

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{KillSwitches, Materializer}
import akka.{NotUsed, actor}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}
import msocket.api.models.HttpError
import msocket.impl.Encoding.JsonText

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class SseTransport[Req: Encoder: ErrorProtocol](uri: String)(implicit actorSystem: ActorSystem[_]) extends Transport[Req] {

  implicit val ec: ExecutionContext               = actorSystem.executionContext
  implicit val system: actor.ActorSystem          = actorSystem.toClassic
  private implicit val materializer: Materializer = Materializer(actorSystem)

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport"))
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .map(event => JsonText.decodeWithServiceError(event.data))
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    val payloadHeader = QueryHeader(JsonText.encode(request))
    val httpRequest   = HttpRequest(HttpMethods.GET, uri = uri, headers = List(payloadHeader))
    Http().singleRequest(httpRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK => Future.successful(response)
        case statusCode     => Unmarshal(response).to[String].map(msg => throw HttpError(statusCode.intValue(), statusCode.reason(), msg))
      }
    }
  }

}
