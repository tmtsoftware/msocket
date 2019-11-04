package msocket.api

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models.{StreamStatus, Subscription}
import msocket.api.utils.InterceptedTransport

import scala.concurrent.Future

abstract class Transport[Req: Encoder] {
  def requestResponse[Res: Decoder](request: Req): Future[Res]
  def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res]

  def requestStream[Res: Decoder](request: Req): Source[Res, Subscription]
  def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]]

  def interceptRequest(action: Req => Unit): Transport[Req] = new InterceptedTransport(this, action)
}
