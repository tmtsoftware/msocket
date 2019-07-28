package msocket.core.server

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.{Encoding, MSocket, PSocket, Payload}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class WsServerFlow[T: Decoder: Encoder, RR <: T: ClassTag, RS <: T: ClassTag](socket: MSocket[RR, RS])(
    implicit mat: Materializer,
    ec: ExecutionContext,
    encoding: Encoding
) {

  private val handler: ServerHandler[T, RR, RS] = new ServerHandler(new PSocket(socket))

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .mapAsync(1000) {
        case message: TextMessage.Strict =>
          handler.handle(encoding.decode[Payload[T]](message.text)).map(List(_))
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
