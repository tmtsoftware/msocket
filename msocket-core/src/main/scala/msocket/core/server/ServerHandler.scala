package msocket.core.server

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import io.bullet.borer.{Decoder, Target}
import msocket.core.api.{PSocket, Payload}
import msocket.core.extensions.ToMessage.ValueToMessage

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ServerHandler[T: Decoder, RR <: T: ClassTag, RS <: T: ClassTag](socket: PSocket[RR, RS])(
    implicit ec: ExecutionContext,
    target: Target
) {
  def handle(payload: Payload[T]): Future[Message] = payload.response.value match {
    case x: RR =>
      val payloadFuture = socket.requestResponse(payload.asInstanceOf[Payload[RR]])
      payloadFuture.map(_.textMessage)
    case x: RS =>
      val payloadStream = socket.requestStream(payload.asInstanceOf[Payload[RS]])
      Future.successful(TextMessage.Streamed(payloadStream.map(_.text)))
  }
}
