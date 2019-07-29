package msocket.core.server

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.Flow
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.Encoding
import msocket.core.api.Encoding.JsonText

class WsServerFlow[T: Decoder: Encoder](socket: ServerSocket[T])(implicit encoding: Encoding) {

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) =>
          encoding.strictMessageStream(socket.requestStream(JsonText.decodeText(text).value))
      }
      .flatMapConcat(identity)
  }

}
