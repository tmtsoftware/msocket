package msocket.core.server

import akka.http.scaladsl.model.ws.Message
import msocket.core.api.{Encoding, MSocket, Payload}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ServerHandler[RR: ClassTag, RS: ClassTag](socket: MSocket[RR, RS])(
    implicit ec: ExecutionContext,
    encoding: Encoding
) {
  def handle(payload: Payload[_]): Future[Message] = payload.response.value match {
    case x: RR => socket.requestResponse(x).map(Payload(_, payload.id)).map(encoding.strict)
    case x: RS => Future.successful(encoding.streamed(socket.requestStream(x).map(Payload(_, payload.id))))
  }
}
