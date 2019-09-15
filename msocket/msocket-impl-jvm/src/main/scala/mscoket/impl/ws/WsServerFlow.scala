package mscoket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import mscoket.impl.ws.Encoding.JsonText
import msocket.api.{RequestHandler, WebsocketEvent}

import scala.concurrent.Future

class WsServerFlow[T: Decoder](websocketClient: RequestHandler[T, Source[String, NotUsed]]) {
  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) => JsonText.decodeText[WebsocketEvent](text)
      }
      .mapAsyncUnordered(10000) { event =>
        println(event)
        val results = websocketClient.handle(JsonText.decodeText(event.data))
        Future.successful(JsonText.streamingMessage(event.id.toString, results))
      }
  }
}
