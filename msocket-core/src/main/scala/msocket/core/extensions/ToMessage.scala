package msocket.core.extensions

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import io.bullet.borer.{Encoder, Json}
import io.bullet.borer.compat.akka._

import scala.concurrent.{ExecutionContext, Future}

object ToMessage {
  implicit class ValueToMessage[T: Encoder](x: T) {
    def byteString: ByteString = Json.encode(x).to[ByteString].result
    def text: String           = byteString.utf8String

    def textMessage: TextMessage.Strict     = TextMessage.Strict(text)
    def binaryMessage: BinaryMessage.Strict = BinaryMessage.Strict(byteString)
  }
  implicit class FlowToMessageFlow[T: Encoder](x: Future[T]) {
    def textMessageFlow: Flow[Message, TextMessage, NotUsed] =
      Flow.fromSinkAndSource(Sink.ignore, Source.fromFuture(x).map(_.textMessage))
  }
  implicit class SourceToMessage[T: Encoder, Mat](stream: Source[T, Mat]) {
    def textMessage: TextMessage.Streamed     = TextMessage.Streamed(stream.map(_.text))
    def binaryMessage: BinaryMessage.Streamed = BinaryMessage.Streamed(stream.map(_.byteString))
  }
  implicit class FutureToMessage[T: Encoder](future: Future[T]) {
    def textMessage(implicit ec: ExecutionContext): Future[TextMessage.Strict]     = future.map(_.textMessage)
    def binaryMessage(implicit ec: ExecutionContext): Future[BinaryMessage.Strict] = future.map(_.binaryMessage)
  }
}
