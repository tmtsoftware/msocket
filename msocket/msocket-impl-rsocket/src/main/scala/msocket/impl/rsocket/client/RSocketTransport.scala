package msocket.impl.rsocket.client

import akka.actor.typed.ActorSystem
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import io.bullet.borer.{Decoder, Encoder, Json}
import io.rsocket.RSocket
import io.rsocket.util.DefaultPayload
import msocket.impl.StreamSplitter._
import msocket.api.Transport
import msocket.api.models.{Result, StreamError, StreamStatus, Subscription}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class RSocketTransport[Req: Encoder](rSocket: RSocket)(implicit actorSystem: ActorSystem[_]) extends Transport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponse(request, 1.hour)
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    val value = rSocket.requestStream(DefaultPayload.create(Json.encode(request).toByteBuffer))
    Source
      .fromPublisher(value)
      .map(x => Json.decode(x.getData).to[Res].value)
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }

  override def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    requestStream[Result[Res, StreamError]](request).split
  }

}
