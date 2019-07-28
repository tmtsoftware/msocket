package msocket.core.api

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

trait MSocket[RR, RS] {
  def requestResponse(message: RR): Future[Payload[_]]
  def requestStream(message: RS): Source[Payload[_], NotUsed]
}
