package msocket.core.api

import java.util.UUID

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}

import scala.concurrent.Future

trait MSocket[RR, RS] {
  def requestResponse(message: RR, id: UUID): Future[Message]
  def requestStream(message: RS, id: UUID): Message
}

trait TextSocket[RR, RS] extends MSocket[RR, RS] {
  def requestResponse(message: RR, id: UUID): Future[TextMessage.Strict]
  def requestStream(message: RS, id: UUID): TextMessage.Streamed
}

trait BinarySocket[RR, RS] extends MSocket[RR, RS] {
  def requestResponse(message: RR, id: UUID): Future[BinaryMessage.Strict]
  def requestStream(message: RS, id: UUID): BinaryMessage.Streamed
}
