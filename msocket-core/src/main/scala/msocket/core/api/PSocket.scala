package msocket.core.api

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContext, Future}

class PSocket[RR, RS](socket: MSocket[RR, RS])(implicit ec: ExecutionContext) {
  def requestResponse(payload: Payload[RR]): Future[Payload[_]] = {
    socket.requestResponse(payload.response.value).map(x => Payload(x, payload.id))
  }

  def requestStream(payload: Payload[RS]): Source[Payload[Any], NotUsed] = {
    socket.requestStream(payload.response.value).map(x => Payload(x.asInstanceOf[MResponse[Any]], payload.id))
  }
}
