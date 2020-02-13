package msocket.impl.sse

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

import scala.concurrent.duration.DurationLong

/**
 * This helper class can be extended to define custom SSe routes[[akka.http.scaladsl.server.StandardRoute]] handler in the server.
 * SseHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class SseHandler[Req: ErrorProtocol]
    extends ServerStreamingSupport[Req, ServerSentEvent](new ServerSentEventEncoder[Req])
    with MessageHandler[Req, Route] {

  override def stream[Res: Encoder, Mat](response: Source[Res, Mat]): Source[ServerSentEvent, NotUsed] =
    super.stream(response).keepAlive(30.seconds, () => ServerSentEvent.heartbeat)
}
