package msocket.core.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.Encoding.JsonText
import msocket.core.api.{Encoding, Payload}

import scala.concurrent.Future

class ClientSocketImpl[RR: Encoder, RS: Encoder](webSocketRequest: WebSocketRequest)(
    implicit actorSystem: ActorSystem,
    encoding: Encoding
) extends ClientSocket[RR, RS] {

  private val setup              = new ClientSocketSetup(webSocketRequest)
  implicit val mat: Materializer = ActorMaterializer()

  override def requestResponse[Res: Decoder: Encoder](request: RR): Future[Res] = {
    setup
      .request(JsonText.strict(Payload(request)))
      .collect {
        case TextMessage.Strict(text)   => JsonText.decodeText(text).value
        case BinaryMessage.Strict(data) => encoding.decodeBinary(data).value
      }
      .runWith(Sink.head)
  }

  override def requestStream[Res: Decoder: Encoder](request: RS): Source[Res, NotUsed] = {
    setup
      .request(JsonText.strict(Payload(request)))
      .collect {
        case TextMessage.Streamed(textStream)   => textStream.map(x => JsonText.decodeText(x).value)
        case BinaryMessage.Streamed(dataStream) => dataStream.map(x => encoding.decodeBinary(x).value)
      }
      .flatMapConcat(identity)
  }
}
