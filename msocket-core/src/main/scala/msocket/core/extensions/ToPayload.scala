package msocket.core.extensions

import java.util.UUID

import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Source
import io.bullet.borer.{Encoder, Target}
import msocket.core.api.Payload
import msocket.core.extensions.ToMessage.{FutureToMessage, SourceToMessage}

import scala.concurrent.{ExecutionContext, Future}

object ToPayload {
  implicit class FutureToPayload[T: Encoder](future: Future[T])(implicit target: Target, ec: ExecutionContext) {
    def payload(id: UUID)(implicit ec: ExecutionContext): Future[Payload[T]]                    = future.map(Payload(_, id))
    def payloadTextMessage(id: UUID)(implicit ec: ExecutionContext): Future[TextMessage.Strict] = payload(id).textMessage
  }

  implicit class SourceToPayload[T: Encoder, Mat](stream: Source[T, Mat])(implicit target: Target) {
    def payload(id: UUID): Source[Payload[T], Mat]         = stream.map(Payload(_, id))
    def payloadTextMessage(id: UUID): TextMessage.Streamed = payload(id).textMessage
  }
}
