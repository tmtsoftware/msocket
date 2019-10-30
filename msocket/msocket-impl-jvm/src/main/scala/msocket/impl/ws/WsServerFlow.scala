package msocket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import msocket.api.MessageHandler
import msocket.api.models.{Result, StreamError}
import msocket.impl.ws.Encoding.{CborBinary, JsonText}

import scala.concurrent.duration.DurationLong
import scala.util.control.NonFatal

class WsServerFlow[T: Decoder](messageHandler: Encoding[_] => MessageHandler[T, Source[Message, NotUsed]])(implicit mat: Materializer) {

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .take(1)
      .mapAsync(1) {
        case msg: TextMessage   => msg.toStrict(100.millis)
        case msg: BinaryMessage => msg.toStrict(100.millis)
      }
      .flatMapConcat {
        case msg: TextMessage   => handle(msg.getStrictText, JsonText)
        case msg: BinaryMessage => handle(msg.getStrictData, CborBinary)
      }
  }

  private def handle[E](text: E, encoding: Encoding[E]): Source[Message, NotUsed] = {
    try {
      val request = encoding.decode(text)
      messageHandler(encoding).handle(request)
    } catch {
      case NonFatal(ex) =>
        val error: Result[Unit, StreamError] = Result.Error(StreamError(ex.getClass.getSimpleName, ex.getMessage))
        Source.single(encoding.strictMessage(error))
    }
  }

}