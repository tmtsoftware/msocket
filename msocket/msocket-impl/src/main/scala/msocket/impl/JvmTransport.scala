package msocket.impl

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Sink
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}
import msocket.portable.Observer

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

abstract class JvmTransport[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) extends Transport[Req] {
  import actorSystem.executionContext

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {
    requestStream(request)
      .map(x => observer.onNext(x))
      .watchTermination() { (subscription, completionF) =>
        completionF.onComplete {
          case Failure(exception) => observer.onError(exception)
          case Success(_)         => observer.onCompleted()
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
