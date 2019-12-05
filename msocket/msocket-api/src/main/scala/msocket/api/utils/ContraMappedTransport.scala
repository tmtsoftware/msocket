package msocket.api.utils

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorType, Transport}
import msocket.api.models.Subscription

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ContraMappedTransport[A, B: Encoder: ErrorType](transport: Transport[A], action: B => A) extends Transport[B] {
  override def requestResponse[Res: Decoder](request: B): Future[Res] = {
    transport.requestResponse(action(request))
  }

  override def requestResponse[Res: Decoder](request: B, timeout: FiniteDuration): Future[Res] = {
    transport.requestResponse(action(request), timeout)
  }

  override def requestStream[Res: Decoder](request: B): Source[Res, Subscription] = {
    transport.requestStream(action(request))
  }
}
