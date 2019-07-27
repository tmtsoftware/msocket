package msocket.core.server

import akka.http.scaladsl.model.ws.Message
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer.{Decoder, Target}
import msocket.core.api.{MSocket, Payload}

import scala.concurrent.Future
import scala.reflect.ClassTag

class ServerHandler[T: Decoder, RR <: T: ClassTag, RS <: T: ClassTag](socket: MSocket[RR, RS])(implicit target: Target) {
  def handle(text: String): Future[Message] = {
    val payload = target.decode(ByteString(text)).to[Payload[T]].value
    payload.message match {
      case x: RR => socket.requestResponse(x, payload.id)
      case x: RS => Future.successful(socket.requestStream(x, payload.id))
    }
  }
}
