package msocket.core.server

import akka.http.scaladsl.model.ws.Message
import io.bullet.borer.Decoder
import msocket.core.api.{Encoding, PSocket, Payload}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ServerHandler[T: Decoder, RR <: T: ClassTag, RS <: T: ClassTag](socket: PSocket[RR, RS])(
    implicit ec: ExecutionContext,
    encoding: Encoding
) {
  def handle(payload: Payload[T]): Future[Message] = payload.response.value match {
    case x: RR => socket.requestResponse(payload.asInstanceOf[Payload[RR]]).map(x => encoding.strict(x))
    case x: RS => Future.successful(encoding.streamed(socket.requestStream(payload.asInstanceOf[Payload[RS]])))
  }
}
