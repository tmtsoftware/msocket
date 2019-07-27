package msocket.core.extensions

import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer.{Encoder, Target}

import scala.concurrent.{ExecutionContext, Future}

object ToMessage {
  implicit class ValueToMessage[T: Encoder](x: T)(implicit target: Target) {
    def byteString: ByteString = target.encode(x).to[ByteString].result
    def text: String           = byteString.utf8String

    def textMessage: TextMessage.Strict     = TextMessage.Strict(text)
    def binaryMessage: BinaryMessage.Strict = BinaryMessage.Strict(byteString)
  }

  implicit class SourceToMessage[T: Encoder, Mat](stream: Source[T, Mat])(implicit target: Target) {
    def textMessage: TextMessage.Streamed     = TextMessage.Streamed(stream.map(_.text))
    def binaryMessage: BinaryMessage.Streamed = BinaryMessage.Streamed(stream.map(_.byteString))
  }
  implicit class FutureToMessage[T: Encoder](future: Future[T])(implicit target: Target, ec: ExecutionContext) {
    def textMessage: Future[TextMessage.Strict]     = future.map(_.textMessage)
    def binaryMessage: Future[BinaryMessage.Strict] = future.map(_.binaryMessage)
  }
}
