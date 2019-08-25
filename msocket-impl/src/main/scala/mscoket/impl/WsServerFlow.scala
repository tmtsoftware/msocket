package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.Flow
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.WebsocketHandler

class WsServerFlow[T: Decoder: Encoder](websocketClient: WebsocketHandler[T]) {

  def flow(encoding: Encoding): Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .collect {
        case BinaryMessage.Strict(data) if encoding.isBinary => encoding.decodeBinary(data)
        case TextMessage.Strict(text) if !encoding.isBinary  => encoding.decodeText(text)
      }
      .flatMapConcat(payload => encoding.strictMessageStream(websocketClient.handle(payload.value)))
  }
}
