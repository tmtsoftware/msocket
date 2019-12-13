package msocket.impl.streaming

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise, TimeoutException}
import scala.scalajs.js.timers

class StreamingTransportJs[Req: Encoder: ErrorProtocol](connector: Connector[Req]) extends Transport[Req] {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport"))
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    val promise: Promise[Res] = Promise()

    val connectedSource = new ConnectedSource().start(request, connector)
    connectedSource.foreach { response =>
      connectedSource.cancel()
      promise.trySuccess(response)
    }

    timers.setTimeout(timeout) {
      connectedSource.cancel()
      promise.tryFailure(new TimeoutException(s"no response obtained within timeout of $timeout"))
    }

    promise.future
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    new ConnectedSource().start(request, connector)
  }

}
