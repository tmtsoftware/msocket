package msocket.impl.post

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

import scala.concurrent.duration.DurationLong

/**
 * This helper class can be extended to define custom HTTP streaming source handler in the server.
 * HttpStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class HttpStreamHandler[Req: ErrorProtocol]
    extends ServerStreamingSupport[Req, FetchEvent](new FetchEventEncoder[Req])
    with MessageHandler[Req, Source[FetchEvent, NotUsed]] {

  override def stream[Res: Encoder, Mat](response: Source[Res, Mat]): Source[FetchEvent, NotUsed] =
    super.stream(response).keepAlive(30.seconds, () => FetchEvent.Heartbeat)
}
