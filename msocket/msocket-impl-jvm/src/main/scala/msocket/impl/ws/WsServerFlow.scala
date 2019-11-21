package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import msocket.api.MessageHandler
import msocket.api.models.{MSocketErrorFrame, StreamError}
import msocket.impl.Encoding
import msocket.impl.Encoding.{CborBinary, JsonText}

import scala.concurrent.duration.DurationLong
import scala.util.control.NonFatal

class WsServerFlow[T: Decoder](messageHandler: Encoding[_] => MessageHandler[T, Source[Message, NotUsed]])(
    implicit actorSystem: ActorSystem[_]
) {

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

  private def handle[E](element: E, encoding: Encoding[E]): Source[Message, NotUsed] = {
    val stream = try {
      val request = encoding.decode(element)
      messageHandler(encoding).handle(request)
    } catch {
      case NonFatal(ex) => Source.failed(ex)
    }
    stream.recover {
      case NonFatal(ex) => encoding.strictMessage(MSocketErrorFrame(StreamError(ex.getClass.getSimpleName, ex.getMessage)))
    }
  }

}
