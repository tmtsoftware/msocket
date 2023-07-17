package msocket.http.post.streaming

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ErrorProtocol
import msocket.api.models.ResponseHeaders
import msocket.security.AccessController
import msocket.jvm.metrics.MetricCollector
import msocket.jvm.stream.{StreamResponse, StreamResponseEncoder}

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

/**
 * This helper class can be extended to define custom HTTP streaming source handler in the server.
 * HttpStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
class HttpStreamResponseEncoder[Req: ErrorProtocol](val accessController: AccessController) extends StreamResponseEncoder[Req, FetchEvent] {
  override def encodeStream(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[FetchEvent, NotUsed] = {
    super.encodeStream(streamResponseF, collector).keepAlive(30.seconds, () => FetchEvent.Heartbeat)
  }

  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): FetchEvent =
    FetchEvent(
      JsonText.encode(response),
      headers.errorType.map(_.name)
    )
}
