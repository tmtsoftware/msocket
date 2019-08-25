package msocket.api

import akka.NotUsed
import akka.stream.scaladsl.Source

trait WebsocketHandler[Req] {
  def handle(request: Req): Source[Payload[_], NotUsed]
}
