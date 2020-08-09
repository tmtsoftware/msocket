package msocket.impl.sse

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.{NotUsed, actor}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType.Json
import msocket.api.SourceExtension.WithSubscription
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.{HttpUtils, JvmTransport}

import scala.concurrent.{ExecutionContext, Future}

class SseTransport[Req: Encoder: ErrorProtocol](uri: String)(implicit actorSystem: ActorSystem[_]) extends JvmTransport[Req] {

  implicit val ec: ExecutionContext               = actorSystem.executionContext
  implicit val system: actor.ActorSystem          = actorSystem.toClassic
  private implicit val materializer: Materializer = Materializer(actorSystem)

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .map(event => JsonText.decodeWithError[Res, Req](event.data))
      .withSubscription()
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    val payloadHeader = QueryHeader(JsonText.encode(request))
    val httpRequest   = HttpRequest(HttpMethods.GET, uri = uri, headers = List(payloadHeader))
    new HttpUtils[Req](Json).handleRequest(httpRequest)
  }

}
