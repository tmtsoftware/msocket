package msocket.core.extensions

import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer.{Encoder, Target}

object ToMessage {
  implicit class ValueToMessage[T: Encoder](x: T)(implicit target: Target) {
    def byteString: ByteString = target.encode(x).to[ByteString].result
    def text: String           = byteString.utf8String

    def textMessage: TextMessage.Strict     = TextMessage.Strict(text)
    def binaryMessage: BinaryMessage.Strict = BinaryMessage.Strict(byteString)
  }
}
