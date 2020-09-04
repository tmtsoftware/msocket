package msocket.impl.sse

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.models.ResponseHeaders
import msocket.api.{ErrorProtocol, StreamResponse}
import msocket.impl.StreamResponseEncoder
import msocket.impl.metrics.MetricCollector
import msocket.security.api.AccessController

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

/**
 * This helper class can be extended to define custom SSE handler in the server which returns [[Source]] of [[ServerSentEvent]].
 * SseHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
class SseStreamResponseEncoder[Req: ErrorProtocol](val accessController: AccessController)
    extends StreamResponseEncoder[Req, ServerSentEvent] {
  override def handle(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[ServerSentEvent, NotUsed] = {
    super.handle(streamResponseF, collector).keepAlive(30.seconds, () => ServerSentEvent.heartbeat)
  }

  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): ServerSentEvent = ServerSentEvent(JsonText.encode(response))
}
