package msocket.impl.streaming

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Transport
import msocket.api.models.{StreamStatus, Subscription}

import scala.scalajs.js.timers
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise, TimeoutException}

abstract class StreamingTransportJs[Req: Encoder](connectionFactory: ConnectionFactory[Req]) extends Transport[Req] {

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    val promise: Promise[Res] = Promise()
    val connectedSource       = connectionFactory.connect(request, new PlainConnectedSource)
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
    connectionFactory.connect(request, new PlainConnectedSource)
  }

  override def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    connectionFactory.connect(request, new ConnectedSourceWithStatus)
  }
}
