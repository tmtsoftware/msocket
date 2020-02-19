package msocket.impl.post

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ErrorProtocol
import msocket.impl.StreamHandler

import scala.concurrent.duration.DurationLong

/**
 * This helper class can be extended to define custom HTTP streaming source handler in the server.
 * HttpStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class HttpStreamHandler[Req: ErrorProtocol] extends StreamHandler[Req, FetchEvent] {
  override def stream[Res: Encoder, Mat](response: Source[Res, Mat]): Source[FetchEvent, NotUsed] =
    super.stream(response).keepAlive(30.seconds, () => FetchEvent.Heartbeat)

  override def encode[Res: Encoder](response: Res): FetchEvent = FetchEvent(JsonText.encode(response))
}
