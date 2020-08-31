package msocket.impl

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}
import msocket.portable.Observer

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise, TimeoutException}
import scala.scalajs.js.timers
import scala.util.{Failure, Success, Try}

abstract class JsTransport[Req: Encoder: ErrorProtocol](implicit ec: ExecutionContext) extends Transport[Req] {
  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    new ConnectedSource(request, this)
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    val promise: Promise[Res] = Promise()

    def messageHandler(message: Try[Option[Res]]): Unit =
      message match {
        case Failure(exception)   => promise.tryFailure(exception)
        case Success(Some(value)) => promise.trySuccess(value)
        case Success(None)        =>
      }

    val subscription = requestStream[Res](request, Observer.from(messageHandler))

    timers.setTimeout(timeout) {
      promise.tryFailure(new TimeoutException(s"no response obtained within timeout of $timeout"))
    }

    promise.future.onComplete(_ => subscription.cancel())
    promise.future
  }
}
