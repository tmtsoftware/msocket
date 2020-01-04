package msocket.impl

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Sink
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

abstract class JvmTransport[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) extends Transport[Req] {
  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription = {
    val stream = requestStream(request).map(onMessage).recover {
      case NonFatal(ex) => onError(ex)
    }
    stream.to(Sink.ignore).run()
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }
}
