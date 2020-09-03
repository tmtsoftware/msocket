package msocket.impl.rsocket.client

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import io.rsocket.RSocket
import msocket.api.SourceExtension.WithSubscription
import msocket.api.models.ResponseHeaders
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.impl.JvmTransport
import msocket.impl.rsocket.RSocketExtensions._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.CompletionStageOps

class RSocketTransport[Req: Encoder: ErrorProtocol](rSocket: RSocket, contentType: ContentType)(implicit actorSystem: ActorSystem[_])
    extends JvmTransport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    rSocket.requestResponse(contentType.payload(request, ResponseHeaders())).toFuture.asScala.map { payload =>
      contentType.response[Res, Req](payload)
    }
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val value = rSocket.requestStream(contentType.payload(request, ResponseHeaders()))
    Source
      .fromPublisher(value)
      .map(payload => contentType.response[Res, Req](payload))
      .withSubscription()
  }

  def subscription(): Subscription = () => rSocket.dispose()
}
