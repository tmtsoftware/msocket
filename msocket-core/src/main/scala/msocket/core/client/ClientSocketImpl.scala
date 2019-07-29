package msocket.core.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.Encoding.{JsonBinary, JsonText}
import msocket.core.api.Payload

class ClientSocketImpl[Req: Encoder](webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem)
    extends ClientSocket[Req] {

  private val setup = new ClientSocketSetup(webSocketRequest)

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, NotUsed] = {
    println(JsonText.strictMessage(Payload(request)))
    setup
      .request(JsonText.strictMessage(Payload(request)))
      .collect {
        case TextMessage.Strict(text)   => JsonText.decodeText(text).value
        case BinaryMessage.Strict(data) => JsonBinary.decodeBinary(data).value
      }
  }
}
