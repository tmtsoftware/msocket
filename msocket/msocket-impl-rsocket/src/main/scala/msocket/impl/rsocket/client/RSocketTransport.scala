package msocket.impl.rsocket.client

import akka.actor.typed.ActorSystem
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import io.rsocket.RSocket
import msocket.api.{Encoding, ErrorProtocol, Subscription}
import msocket.impl.JvmTransport
import msocket.impl.rsocket.RSocketExtensions._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.CompletionStageOps

class RSocketTransport[Req: Encoder: ErrorProtocol](rSocket: RSocket, encoding: Encoding[_])(implicit actorSystem: ActorSystem[_])
    extends JvmTransport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    rSocket.requestResponse(encoding.payload(request)).toFuture.asScala.map { payload =>
      encoding.response[Res, Req](payload)
    }
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val value = rSocket.requestStream(encoding.payload(request))
    Source
      .fromPublisher(value)
      .map(payload => encoding.response[Res, Req](payload))
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }
}
