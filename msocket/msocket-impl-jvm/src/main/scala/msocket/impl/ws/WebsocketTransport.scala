package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models.Subscription
import msocket.api.{ErrorType, Transport}
import msocket.impl.Encoding
import msocket.impl.Encoding.{CborBinary, JsonText}

import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

class WebsocketTransport[Req: Encoder: ErrorType](uri: String, encoding: Encoding[_])(
    implicit actorSystem: ActorSystem[_]
) extends Transport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  private val setup = new WebsocketTransportSetup(WebSocketRequest(uri))

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport"))
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream[Res](request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] =
    setup
      .request(encoding.strictMessage(request))
      .mapAsync(16) {
        case msg: TextMessage   => msg.toStrict(100.millis).map(m => JsonText.decodeWithError[Res, Req](m.text))
        case msg: BinaryMessage => msg.toStrict(100.millis).map(m => CborBinary.decodeWithError[Res, Req](m.data))
      }
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue[Subscription](switch => () => switch.shutdown())
}
