package mscoket.impl.rsocket.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder, Json}
import io.rsocket.RSocket
import io.rsocket.util.DefaultPayload
import mscoket.impl.StreamSplitter._
import msocket.api.Transport
import msocket.api.utils.{Result, StreamStatus}

import scala.concurrent.{ExecutionContext, Future}

class RSocketClient[Req: Encoder](rSocket: RSocket)(implicit actorSystem: ActorSystem) extends Transport[Req] {

  implicit lazy val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext   = actorSystem.dispatcher

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }

  override def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    val value = rSocket.requestStream(DefaultPayload.create(Json.encode(request).toByteBuffer))
    Source.fromPublisher(value).map(x => Json.decode(x.getData).to[Res].value)
  }

  override def requestStreamWithError[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    requestStream[Result[Res, StreamStatus]](request).split
  }

}
