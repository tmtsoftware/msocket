package msocket.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}

import scala.concurrent.Future

abstract class RequestClient[Req: Encoder] {
  def requestResponse[Res: Decoder](request: Req): Future[Res]
  def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed]
  def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]]
}
