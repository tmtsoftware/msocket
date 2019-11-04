package msocket.impl.post

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Encoder, Json}
import msocket.api.models.FetchEvent
import msocket.impl.StreamExtensions

import scala.concurrent.duration.DurationLong

trait PostStreamExtensions extends StreamExtensions[FetchEvent] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[FetchEvent, NotUsed] = {
    input
      .map(x => FetchEvent(Json.encode(x).toUtf8String))
      .keepAlive(30.seconds, () => FetchEvent.Heartbeat)
      .mapMaterializedValue(_ => NotUsed)
  }
}
