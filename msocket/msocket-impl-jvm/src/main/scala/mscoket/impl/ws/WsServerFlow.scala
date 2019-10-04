package mscoket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import mscoket.impl.ws.Encoding.JsonText
import msocket.api.MessageHandler
import msocket.api.models.{Result, StreamError}

import scala.concurrent.duration.DurationLong
import scala.util.control.NonFatal

class WsServerFlow[T: Decoder](messageHandler: MessageHandler[T, Source[Message, NotUsed]])(implicit mat: Materializer) {

  import mat.executionContext

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .take(1)
      .mapAsync(16) {
        case msg: TextMessage   => msg.toStrict(100.millis).map(_.text)
        case msg: BinaryMessage => throw new RuntimeException("websocket transport currently does not handle binary messages")
      }
      .flatMapConcat(handle)
  }

  private def handle(text: String): Source[Message, NotUsed] = {
    try {
      val request = JsonText.decodeText(text)
      messageHandler.handle(request)
    } catch {
      case NonFatal(ex) =>
        val error: Result[Unit, StreamError] = Result.Error(StreamError(ex.getClass.getSimpleName, ex.getMessage))
        Source.single(JsonText.strictMessage(error))
    }
  }
}
