package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Transport
import msocket.api.models.{Result, StreamError, StreamStatus, Subscription}
import msocket.impl.Encoding
import msocket.impl.Encoding.{CborBinary, JsonText}
import msocket.impl.StreamSplitter._

import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

class WebsocketTransport[Req: Encoder](uri: String, encoding: Encoding[_])(implicit actorSystem: ActorSystem[_]) extends Transport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  private val setup = new WebsocketTransportSetup(WebSocketRequest(uri))

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponse(request, 1.hour)
  }

  override def requestResponse[Res: Decoder](request: Req, timeout: FiniteDuration): Future[Res] = {
    requestStream(request).completionTimeout(timeout).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, Subscription] =
    setup
      .request(encoding.strictMessage(request))
      .mapAsync(16) {
        case msg: TextMessage   => msg.toStrict(100.millis).map(m => JsonText.decode(m.text))
        case msg: BinaryMessage => msg.toStrict(100.millis).map(m => CborBinary.decode(m.data))
      }
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())

  override def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    requestStream[Result[Res, StreamError]](request).split
  }

}
