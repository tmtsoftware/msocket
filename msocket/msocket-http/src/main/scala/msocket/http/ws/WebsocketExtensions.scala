package msocket.http.ws

import org.apache.pekko.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType
import msocket.api.ContentType.{Cbor, Json}
import msocket.http.CborByteString

object WebsocketExtensions {

  implicit class WebsocketEncoding(contentType: ContentType) {
    def strictMessage[T: Encoder](input: T): Message =
      contentType match {
        case Json => TextMessage.Strict(JsonText.encode(input))
        case Cbor => BinaryMessage.Strict(CborByteString.encode(input))
      }
  }
}
