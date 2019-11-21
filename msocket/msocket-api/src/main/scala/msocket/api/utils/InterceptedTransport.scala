package msocket.api.utils

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Transport
import msocket.api.models.Subscription

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class InterceptedTransport[Req: Encoder](transport: Transport[Req], action: Req => Unit) extends Transport {
  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    action(request)
    transport.requestResponse(request)
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    action(request)
    transport.requestResponse(request, timeout)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] = {
    action(request)
    transport.requestStream(request)
  }
}
