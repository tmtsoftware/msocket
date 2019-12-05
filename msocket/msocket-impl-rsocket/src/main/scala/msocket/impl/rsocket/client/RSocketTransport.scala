package msocket.impl.rsocket.client

import akka.actor.typed.ActorSystem
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import io.bullet.borer.{Decoder, Encoder}
import io.rsocket.RSocket
import io.rsocket.util.DefaultPayload
import msocket.api.{ErrorProtocol, Subscription, Transport}
import msocket.impl.Encoding.CborBinary

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class RSocketTransport[Req: Encoder: ErrorProtocol](rSocket: RSocket)(implicit actorSystem: ActorSystem[_]) extends Transport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport"))
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    val value = rSocket.requestStream(DefaultPayload.create(CborBinary.encode(request).toByteBuffer))
    Source
      .fromPublisher(value)
      .map(x => CborBinary.decodeWithServiceError(ByteString.fromByteBuffer(x.getData)))
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }
}
