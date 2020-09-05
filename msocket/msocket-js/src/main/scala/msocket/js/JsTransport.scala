package msocket.js

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}
import msocket.portable.Observer

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise, TimeoutException}
import scala.scalajs.js.timers

abstract class JsTransport[Req: Encoder: ErrorProtocol](implicit ec: ExecutionContext) extends Transport[Req] {
  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    new ConnectedSource(request, this)
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    val promise: Promise[Res] = Promise()
    val subscription          = requestStream[Res](request, Observer.fromPromise(promise))
    timers.setTimeout(timeout) {
      promise.tryFailure(new TimeoutException(s"no response obtained within timeout of $timeout"))
    }

    promise.future.onComplete(_ => subscription.cancel())
    promise.future
  }
}
