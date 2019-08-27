package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.Flow
import io.bullet.borer.{Decoder, Encoder}
import mscoket.impl.Encoding.JsonText
import msocket.api.WebsocketHandler

class WsServerFlow[T: Decoder: Encoder](websocketClient: WebsocketHandler[T]) {

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) => JsonText.decodeText(text)
      }
      .flatMapConcat(payload => JsonText.strictMessageStream(websocketClient.handle(payload.value)))
  }
}
