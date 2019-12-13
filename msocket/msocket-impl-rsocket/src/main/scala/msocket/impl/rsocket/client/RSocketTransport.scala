package msocket.impl.rsocket.client

import akka.actor.typed.ActorSystem
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import io.rsocket.RSocket
import io.rsocket.util.DefaultPayload
import msocket.api.Encoding.{CborByteArray, CborByteBuffer}
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.JvmTransport

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.CompletionStageOps

class RSocketTransport[Req: Encoder: ErrorProtocol](rSocket: RSocket)(implicit actorSystem: ActorSystem[_]) extends JvmTransport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    rSocket.requestResponse(DefaultPayload.create(CborByteBuffer.encode(request))).toFuture.asScala.map { x =>
      CborByteArray.decodeWithError[Res, Req](x.getData.toByteArray)
    }
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    val value = rSocket.requestStream(DefaultPayload.create(CborByteBuffer.encode(request)))
    Source
      .fromPublisher(value)
      .map(x => CborByteArray.decodeWithError[Res, Req](x.getData.toByteArray))
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }
}
