package msocket.core.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}

import scala.concurrent.Future

trait MServerSocket[RR, RS] {
  def requestResponse(request: RR): Future[Payload[_]]
  def requestStream(request: RS): Source[Payload[_], NotUsed]
}

trait MClientSocket[RR, RS] {
  def requestResponse[Res: Decoder: Encoder](request: RR): Future[Payload[Res]]
  def requestStream[Res: Decoder: Encoder](request: RS): Source[Payload[Res], NotUsed]
}
