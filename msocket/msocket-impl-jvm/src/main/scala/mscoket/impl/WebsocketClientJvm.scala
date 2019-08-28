package mscoket.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import mscoket.impl.Encoding.JsonText

import scala.concurrent.Future

class WebsocketClientJvm[Req: Encoder](uri: String)(implicit actorSystem: ActorSystem) extends AbstractClientJvm[Req](uri) {

  private val setup = new WebsocketClientSetup(WebSocketRequest(uri))

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    setup
      .request(JsonText.strictMessage(request))
      .collect {
        case TextMessage.Strict(text) => JsonText.decodeText(text)
      }
  }

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }
}
