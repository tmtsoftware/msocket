package msocket.impl.sse

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.ErrorProtocol
import msocket.impl.StreamHandler

import scala.concurrent.duration.DurationLong

/**
 * This helper class can be extended to define custom SSE handler in the server which returns [[Source]] of [[ServerSentEvent]].
 * SseHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class SseHandler[Req: ErrorProtocol] extends StreamHandler[Req, ServerSentEvent] {
  override def stream[Res: Encoder, Mat](response: Source[Res, Mat]): Source[ServerSentEvent, NotUsed] =
    super.stream(response).keepAlive(30.seconds, () => ServerSentEvent.heartbeat)

  override def encode[Res: Encoder](response: Res): ServerSentEvent = ServerSentEvent(JsonText.encode(response))
}
