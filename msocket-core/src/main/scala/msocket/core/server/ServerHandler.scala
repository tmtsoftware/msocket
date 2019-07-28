package msocket.core.server

import akka.http.scaladsl.model.ws.Message
import msocket.core.api.{Encoding, MServerSocket, Envelope}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ServerHandler[RR: ClassTag, RS: ClassTag](socket: MServerSocket[RR, RS])(
    implicit ec: ExecutionContext,
    encoding: Encoding
) {
  def handle(envelope: Envelope[_]): Future[Message] = envelope.payload.value match {
    case x: RR => socket.requestResponse(x).map(Envelope(_, envelope.id)).map(encoding.strict)
    case x: RS => Future.successful(encoding.streamed(socket.requestStream(x).map(Envelope(_, envelope.id))))
  }
}
