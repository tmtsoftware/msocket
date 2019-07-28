package msocket.core.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.core.api.Payload

import scala.concurrent.Future

trait ServerSocket[RR, RS] {
  def requestResponse(request: RR): Future[Payload[_]]
  def requestStream(request: RS): Source[Payload[_], NotUsed]
}
