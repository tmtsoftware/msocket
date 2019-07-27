package msocket.core.server

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import io.bullet.borer.Decoder

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class WsServerFlow[T: Decoder, RR: ClassTag, RS: ClassTag](handler: ServerHandler[T, RR, RS])(
    implicit mat: Materializer,
    ec: ExecutionContext
) {
  val value: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .mapAsync(1000) {
        case TextMessage.Strict(text) =>
          handler.handle(text).map(List(_))
        case message: TextMessage.Streamed =>
          message.textStream.runWith(Sink.ignore)
          Future.successful(List.empty)
        case message: BinaryMessage =>
          message.dataStream.runWith(Sink.ignore)
          Future.successful(List.empty)
      }
      .mapConcat(identity)
  }
}
