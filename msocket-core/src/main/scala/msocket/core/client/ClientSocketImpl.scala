package msocket.core.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.Encoding.JsonText
import msocket.core.api.{Encoding, Payload}

class ClientSocketImpl[T: Encoder](webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem, encoding: Encoding)
    extends ClientSocket[T] {

  private val setup = new ClientSocketSetup(webSocketRequest)

  override def requestStream[Res: Decoder: Encoder](request: T): Source[Res, NotUsed] = {
    println(JsonText.strictMessage(Payload(request)))
    setup
      .request(JsonText.strictMessage(Payload(request)))
      .collect {
        case TextMessage.Strict(text)   => JsonText.decodeText(text).value
        case BinaryMessage.Strict(data) => encoding.decodeBinary(data).value
      }
  }
}
