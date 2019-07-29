package msocket.core.server

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.Flow
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.Encoding.{JsonBinary, JsonText}

class WsServerFlow[T: Decoder: Encoder](socket: ServerSocket[T]) {

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) =>
          JsonText.strictMessageStream(socket.requestStream(JsonText.decodeText(text).value))
        case BinaryMessage.Strict(data) =>
          JsonBinary.strictMessageStream(socket.requestStream(JsonBinary.decodeBinary(data).value))
      }
      .flatMapConcat(identity)
  }
}
