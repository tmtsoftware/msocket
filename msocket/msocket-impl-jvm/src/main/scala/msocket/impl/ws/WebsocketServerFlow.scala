package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import msocket.api.Encoding.JsonText
import msocket.api.{Encoding, ErrorProtocol, MessageHandler}
import msocket.impl.CborByteString

import scala.concurrent.duration.DurationLong

class WebsocketServerFlow[T: Decoder](messageHandler: Encoding[_] => MessageHandler[T, Source[Message, NotUsed]])(
    implicit actorSystem: ActorSystem[_],
    ep: ErrorProtocol[T]
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
        case msg: BinaryMessage => handle(msg.getStrictData, CborByteString)
      }
  }

  private def handle[E](element: E, encoding: Encoding[E]): Source[Message, NotUsed] = {
    val messageEncoder = new WebsocketMessageEncoder[T](encoding)
    Source
      .lazySingle(() => encoding.decode[T](element))
      .flatMapConcat(messageHandler(encoding).handle)
      .recover(messageEncoder.errorEncoder)
  }
}
