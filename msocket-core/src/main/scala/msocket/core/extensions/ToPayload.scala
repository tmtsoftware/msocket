package msocket.core.extensions

import java.util.UUID

import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.core.api.Payload
import msocket.core.extensions.ToMessage.{FutureToMessage, SourceToMessage}

import scala.concurrent.{ExecutionContext, Future}

object ToPayload {
  implicit class FutureToPayload[T: Encoder](future: Future[T]) {
    def payload(id: UUID)(implicit ec: ExecutionContext): Future[Payload[T]]                    = future.map(Payload(id, _))
    def payloadTextMessage(id: UUID)(implicit ec: ExecutionContext): Future[TextMessage.Strict] = payload(id).textMessage
  }

  implicit class SourceToPayload[T: Encoder, Mat](stream: Source[T, Mat]) {
    def payload(id: UUID): Source[Payload[T], Mat]         = stream.map(Payload(id, _))
    def payloadTextMessage(id: UUID): TextMessage.Streamed = payload(id).textMessage
  }
}
