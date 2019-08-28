package mscoket.impl.sse

import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source
import io.bullet.borer.{Encoder, Json}
import mscoket.impl.StreamExtensions

import scala.concurrent.duration.DurationLong

trait SseStreamExtensions extends StreamExtensions[ServerSentEvent] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[ServerSentEvent, Mat] = {
    input
      .map(x => ServerSentEvent(Json.encode(x).toUtf8String))
      .keepAlive(30.seconds, () => ServerSentEvent.heartbeat)
  }
}
