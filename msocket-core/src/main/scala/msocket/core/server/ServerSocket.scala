package msocket.core.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.core.api.Payload

import scala.concurrent.Future

trait ServerSocket[Req] {
  def requestStream(request: Req): Source[Payload[_], NotUsed]
}
