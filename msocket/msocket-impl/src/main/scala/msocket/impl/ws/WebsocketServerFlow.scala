package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentEncoding, ContentType, Labelled}
import msocket.impl.CborByteString
import msocket.impl.metrics.MetricMetadata
import msocket.impl.metrics.Metrics.withMetrics

import scala.concurrent.duration.DurationLong

class WebsocketServerFlow[T: Decoder: Labelled](
    messageHandler: ContentType => WebsocketHandler[T],
    metadata: MetricMetadata
)(implicit actorSystem: ActorSystem[_]) {

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .take(1)
      .mapAsync(1) {
        case msg: TextMessage   => msg.toStrict(100.millis)
        case msg: BinaryMessage => msg.toStrict(100.millis)
      }
      .flatMapConcat {
        case msg: TextMessage   => handle(msg.getStrictText, JsonText)
        case msg: BinaryMessage => handle(msg.getStrictData, CborByteString)
      }
  }

  private def handle[E](element: E, contentEncoding: ContentEncoding[E]): Source[Message, NotUsed] = {
    val handler = messageHandler(contentEncoding.contentType)
    Source
      .lazySingle(() => contentEncoding.decode[T](element))
      .flatMapConcat { req =>
        val source = handler.handle(req)
        withMetrics(source, req, metadata)
      }
      .recover(handler.errorEncoder)
  }

}
