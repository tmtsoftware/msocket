package msocket.impl

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Sink
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

abstract class JvmTransport[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) extends Transport[Req] {
  override def requestStream[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription = {
    requestStream(req).to(Sink.foreach(onMessage)).run()
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }
}
