package mscoket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import mscoket.impl.ws.Encoding.JsonText
import msocket.api.{RequestHandler, WebsocketEvent}

class WsServerFlow[T: Decoder](websocketClient: RequestHandler[T, Source[String, NotUsed]]) {
  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) => JsonText.decodeText[WebsocketEvent](text)
      }
      .flatMapConcat { event =>
        websocketClient.handle(JsonText.decodeText(event.data)).map { data =>
          JsonText.strictMessage(WebsocketEvent(event.id, data))
        }
      }
  }
}
