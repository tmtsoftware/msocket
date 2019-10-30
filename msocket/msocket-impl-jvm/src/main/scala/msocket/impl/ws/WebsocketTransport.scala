package msocket.impl.ws

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Transport
import msocket.api.models.{Result, StreamError, StreamStatus}
import msocket.impl.StreamSplitter._
import msocket.impl.ws.Encoding.{CborBinary, JsonText}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class WebsocketTransport[Req: Encoder](uri: String, encoding: Encoding[_])(implicit actorSystem: ActorSystem) extends Transport[Req] {

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val setup = new WebsocketTransportSetup(WebSocketRequest(uri))

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }

  override def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] =
    setup
      .request(encoding.strictMessage(request))
      .mapAsync(16) {
        case msg: TextMessage   => msg.toStrict(100.millis).map(m => JsonText.decode(m.text))
        case msg: BinaryMessage => msg.toStrict(100.millis).map(m => CborBinary.decode(m.data))
      }

  override def requestStreamWithStatus[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    requestStream[Result[Res, StreamError]](request).split
  }

}
