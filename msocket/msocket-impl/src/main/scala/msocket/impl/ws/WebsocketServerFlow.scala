package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentEncoding, ContentType, ErrorProtocol, MessageHandler}
import msocket.impl.CborByteString

import scala.concurrent.duration.DurationLong

class WebsocketServerFlow[T: Decoder](messageHandler: ContentType => MessageHandler[T, Source[Message, NotUsed]])(
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

  private def handle[E](element: E, contentEncoding: ContentEncoding[E]): Source[Message, NotUsed] = {
    val messageEncoder = new WebsocketMessageEncoder[T](contentEncoding.contentType)
    Source
      .lazySingle(() => contentEncoding.decode[T](element))
      .flatMapConcat(messageHandler(contentEncoding.contentType).handle)
      .recover(messageEncoder.errorEncoder)
  }
}
