package msocket.api.utils

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Transport
import msocket.api.models.StreamStatus

import scala.concurrent.Future

class InterceptedTransport[Req: Encoder](transport: Transport[Req], action: Req => Unit) extends Transport {
  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    action(request)
    transport.requestResponse(request)
  }

  override def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res] = {
    action(request)
    transport.requestResponseWithDelay(request)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    action(request)
    transport.requestStream(request)
  }

  override def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    action(request)
    transport.requestStreamWithStatus(request)
  }
}
