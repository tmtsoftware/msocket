package msocket.impl.post

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ErrorProtocol, StreamResponse}
import msocket.impl.StreamHandler
import msocket.impl.metrics.MetricCollector

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

/**
 * This helper class can be extended to define custom HTTP streaming source handler in the server.
 * HttpStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
class HttpStreamHandler[Req: ErrorProtocol] extends StreamHandler[Req, FetchEvent] {
  override def handle(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[FetchEvent, NotUsed] = {
    super.handle(streamResponseF, collector).keepAlive(30.seconds, () => FetchEvent.Heartbeat)
  }

  override def encode[Res: Encoder](response: Res): FetchEvent = FetchEvent(JsonText.encode(response))
}
