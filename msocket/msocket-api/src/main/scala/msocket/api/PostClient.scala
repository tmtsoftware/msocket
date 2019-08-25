package msocket.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}

import scala.concurrent.Future

trait PostClient {
  def requestResponse[Req: Encoder, Res: Decoder](req: Req): Future[Res]
  def requestStream[Req: Encoder, Res: Decoder](req: Req): Source[Res, NotUsed]
}
