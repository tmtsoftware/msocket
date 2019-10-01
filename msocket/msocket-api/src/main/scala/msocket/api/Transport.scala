package msocket.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models.StreamStatus

import scala.concurrent.Future

abstract class Transport[Req: Encoder] {
  def requestResponse[Res: Decoder](request: Req): Future[Res]
  def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res]

  def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed]
  def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]]
}
