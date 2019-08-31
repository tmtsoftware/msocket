package mscoket.impl.post

import akka.stream.scaladsl.Source
import io.bullet.borer.{Encoder, Json}
import mscoket.impl.StreamExtensions
import msocket.api.FetchEvent

import scala.concurrent.duration.DurationLong

trait PostStreamExtensions extends StreamExtensions[FetchEvent] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[FetchEvent, Mat] = {
    input
      .map(x => FetchEvent(Json.encode(x).toUtf8String))
      .keepAlive(30.seconds, () => FetchEvent.Heartbeat)
  }
}
