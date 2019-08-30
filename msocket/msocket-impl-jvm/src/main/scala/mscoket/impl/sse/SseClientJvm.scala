package mscoket.impl.sse

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder, Json}
import mscoket.impl.StreamSplitter._
import msocket.api.{RequestClient, Result}

import scala.concurrent.{ExecutionContext, Future}

class SseClientJvm[Req: Encoder](uri: String)(implicit actorSystem: ActorSystem) extends RequestClient[Req] {

  implicit lazy val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext   = actorSystem.dispatcher

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

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    requestStream[Result[Res, Err]](request).split
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    val payloadHeader = QueryHeader(Json.encode(request).toUtf8String)
    val httpRequest   = HttpRequest(HttpMethods.GET, uri = uri, headers = List(payloadHeader))
    Http().singleRequest(httpRequest)
  }

}
