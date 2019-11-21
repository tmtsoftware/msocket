package msocket.impl.rsocket.client

import akka.actor.typed.ActorSystem
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import io.bullet.borer.{Decoder, Encoder, Json}
import io.rsocket.RSocket
import io.rsocket.util.DefaultPayload
import msocket.api.Transport
import msocket.api.models.Subscription
import msocket.impl.Encoding.CborBinary

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class RSocketTransport[Req: Encoder](rSocket: RSocket)(implicit actorSystem: ActorSystem[_]) extends Transport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    throw new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport")
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    val value = rSocket.requestStream(DefaultPayload.create(Json.encode(request).toByteBuffer))
    Source
      .fromPublisher(value)
      .map(x => CborBinary.decodeWithFrameError(ByteString.fromByteBuffer(x.getData)))
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }
}
