package msocket.jvm

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Sink
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}
import msocket.portable.Observer
import msocket.portable.PortableAkka.SourceOps

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

abstract class JvmTransport[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) extends Transport[Req] {
  import actorSystem.executionContext

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {
    requestStream(request).viaObserver(observer).to(Sink.ignore).run()
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }
}
