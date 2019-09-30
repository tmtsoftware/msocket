package mscoket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import mscoket.impl.ws.Encoding.JsonText
import msocket.api.{MessageHandler, StreamError}

import scala.util.control.NonFatal

class WsServerFlow[T: Decoder](websocketClient: MessageHandler[T, Source[Message, NotUsed]]) {

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) =>
          try {
            val request = JsonText.decodeText(text)
            websocketClient.handle(request)
          } catch {
            case NonFatal(ex) =>
              val error = StreamError(ex.getClass.getSimpleName, ex.getMessage)
              Source.single(JsonText.strictMessage(error))
          }
      }
      .flatMapConcat(identity)
  }
}
