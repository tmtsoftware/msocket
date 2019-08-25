package msocket.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}

import scala.concurrent.Future

trait WebsocketClient[Req] {
  def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, NotUsed]
  def requestStreamWithError[Res: Decoder: Encoder, Err: Decoder: Encoder](request: Req): Source[Res, Future[Option[Err]]]
  def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res]
}
