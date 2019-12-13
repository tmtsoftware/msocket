package msocket.api.utils

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription, Transport}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ContraMappedTransport[A, B: Encoder: ErrorProtocol](transport: Transport[A], contraF: B => A) extends Transport[B] {
  override def requestResponse[Res: Decoder](request: B): Future[Res] = {
    transport.requestResponse(contraF(request))
  }

  override def requestResponse[Res: Decoder](request: B, timeout: FiniteDuration): Future[Res] = {
    transport.requestResponse(contraF(request), timeout)
  }

  override def requestStream[Res: Decoder](request: B): Source[Res, Subscription] = {
    transport.requestStream(contraF(request))
  }
}
