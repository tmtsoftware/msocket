package msocket.core.server

import akka.http.scaladsl.model.ws.Message
import io.bullet.borer.{Decoder, Json}
import msocket.core.api.{MSocket, Payload}

import scala.concurrent.Future
import scala.reflect.ClassTag

class ServerHandler[T: Decoder, RR: ClassTag, RS: ClassTag](socket: MSocket[RR, RS]) {
  def handle(text: String): Future[Message] = {
    val payload: Payload[T] = Json.decode(text.getBytes()).to[Payload[T]].value
    payload.message match {
      case x: RR => socket.requestResponse(x, payload.id)
      case x: RS => Future.successful(socket.requestStream(x, payload.id))
    }
  }
}
