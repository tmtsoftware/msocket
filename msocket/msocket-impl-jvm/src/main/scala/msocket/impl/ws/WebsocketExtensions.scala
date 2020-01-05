package msocket.impl.ws

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import io.bullet.borer.Encoder
import msocket.api.Encoding
import msocket.api.Encoding.JsonText
import msocket.impl.CborByteString

object WebsocketExtensions {

  implicit class WebsocketEncoding(encoding: Encoding[_]) {
    def strictMessage[T: Encoder](input: T): Message = encoding match {
      case CborByteString => BinaryMessage.Strict(CborByteString.encode(input))
      case JsonText       => TextMessage.Strict(JsonText.encode(input))
      case _              => throw new RuntimeException(s"websocket transport does not support $encoding encoding")
    }
  }
}
