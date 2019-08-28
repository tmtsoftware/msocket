package mscoket.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.{Decoder, Encoder, Json}

import scala.concurrent.Future

class SseClientJvm[Req: Encoder](uri: String)(implicit actorSystem: ActorSystem) extends AbstractClientJvm[Req](uri) {

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
    val sseStream    = Source.fromFutureSource(futureSource)
    sseStream
      .map(event => Json.decode(event.data.getBytes()).to[Res].value)
      .mapMaterializedValue(_ => NotUsed)
  }

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    val payloadHeader = PayloadHeader(Json.encode(request).toUtf8String)
    val httpRequest   = HttpRequest(HttpMethods.GET, uri = uri, headers = List(payloadHeader))
    Http().singleRequest(httpRequest)
  }

}
