package msocket.core.api

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

trait MSocket[RR, RS] {
  def requestResponse(message: RR): Future[Response[_]]
  def requestStream(message: RS): Source[Response[_], NotUsed]
}
