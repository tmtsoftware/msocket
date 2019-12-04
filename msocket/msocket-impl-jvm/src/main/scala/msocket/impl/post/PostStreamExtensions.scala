package msocket.impl.post

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.models.{FetchEvent, ServiceException}
import msocket.impl.Encoding.JsonText
import msocket.impl.StreamExtensions

import scala.concurrent.duration.DurationLong
import scala.util.control.NonFatal

trait PostStreamExtensions extends StreamExtensions[FetchEvent] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[FetchEvent, NotUsed] = {
    input
      .map(x => FetchEvent(JsonText.encode(x)))
      .keepAlive(30.seconds, () => FetchEvent.Heartbeat)
      .mapMaterializedValue(_ => NotUsed)
      .recover {
        case NonFatal(ex) => FetchEvent(JsonText.encode(ServiceException.fromThrowable(ex)))
      }
  }
}
