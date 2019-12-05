package msocket.impl.streaming

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorType, Transport}
import msocket.api.models.Subscription

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise, TimeoutException}
import scala.scalajs.js.timers

class StreamingTransportJs[Req: Encoder: ErrorType](connectionFactory: ConnectionFactory[Req]) extends Transport[Req] {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport"))
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    val promise: Promise[Res] = Promise()
    val connectedSource       = connectionFactory.connect(request, new ConnectedSource)
    connectedSource.foreach { response =>
      connectedSource.subscription.cancel()
      promise.trySuccess(response)
    }
    timers.setTimeout(timeout) {
      promise.tryFailure(new TimeoutException(s"no response obtained within timeout of $timeout"))
    }
    promise.future
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    connectionFactory.connect(request, new ConnectedSource)
  }

}
