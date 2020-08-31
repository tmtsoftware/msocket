package msocket.impl

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Sink
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

abstract class JvmTransport[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) extends Transport[Req] {
  import actorSystem.executionContext

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Try[Option[Res]] => Unit): Subscription = {
    requestStream(request)
      .map(x => onMessage(Success(Some(x))))
      .watchTermination() { (subscription, completionF) =>
        completionF.onComplete {
          case Failure(exception) => onMessage(Failure(exception))
          case Success(_)         => onMessage(Success(None))
        }
        subscription
      }
      .to(Sink.ignore)
      .run()
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }
}
