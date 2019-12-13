package msocket.impl

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise, TimeoutException}
import scala.scalajs.js.timers

class TransportJs[Req: Encoder: ErrorProtocol](connector: Connector[Req]) extends Transport[Req] {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = connector.requestResponse(request)

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    val promise: Promise[Res] = Promise()

    val connectedSource = requestStream(request)
    connectedSource.foreach { response =>
      connectedSource.materializedValue.cancel()
      promise.trySuccess(response)
    }

    timers.setTimeout(timeout) {
      connectedSource.materializedValue.cancel()
      promise.tryFailure(new TimeoutException(s"no response obtained within timeout of $timeout"))
    }

    promise.future
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    new ConnectedSource().start(request, connector)
  }

}
