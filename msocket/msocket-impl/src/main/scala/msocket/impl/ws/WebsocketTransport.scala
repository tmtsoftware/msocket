package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Source}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.impl.ws.WebsocketExtensions.WebsocketEncoding
import msocket.impl.{CborByteString, JvmTransport}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class WebsocketTransport[Req: Encoder: ErrorProtocol](uri: String, contentType: ContentType)(implicit actorSystem: ActorSystem[_])
    extends JvmTransport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  private val setup = new WebsocketTransportSetup(WebSocketRequest(uri))

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] =
    setup
      .request(contentType.strictMessage(request))
      .mapAsync(16) {
        case msg: TextMessage   => msg.toStrict(100.millis).map(m => JsonText.decodeWithError[Res, Req](m.text))
        case msg: BinaryMessage => msg.toStrict(100.millis).map(m => CborByteString.decodeWithError[Res, Req](m.data))
      }
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue[Subscription](switch => () => switch.shutdown())
}
