package msocket.core.server

import akka.http.scaladsl.model.ws.Message
import msocket.core.api.{Encoding, Payload}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ServerHandler[RR: ClassTag, RS: ClassTag](socket: ServerSocket[RR, RS])(
    implicit ec: ExecutionContext,
    encoding: Encoding
) {
  def handle(payload: Payload[_]): Future[Message] = payload.value match {
    case x: RR => socket.requestResponse(x).map(encoding.strict)
    case x: RS => Future.successful(encoding.streamed(socket.requestStream(x)))
  }
}
