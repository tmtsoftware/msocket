package msocket.core.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}

import scala.concurrent.Future

trait ClientSocket[RR, RS] {
  def requestResponse[Res: Decoder: Encoder](request: RR): Future[Res]
  def requestStream[Res: Decoder: Encoder](request: RS): Source[Res, NotUsed]
}
