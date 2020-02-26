package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import io.prometheus.client.Gauge
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentEncoding, ContentType, Labelled}
import msocket.impl.CborByteString
import msocket.impl.metrics.Metrics.withMetrics

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class WebsocketServerFlow[T: Decoder](
    messageHandler: ContentType => WebsocketHandler[T],
    metricsEnabled: Boolean,
    gauge: => Gauge,
    hostAddress: String
)(implicit actorSystem: ActorSystem[_], labelGen: T => Labelled[T]) {
  import actorSystem.executionContext

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

  private def handle[E](element: E, contentEncoding: ContentEncoding[E]): Source[Message, Future[NotUsed]] = {
    val handler = messageHandler(contentEncoding.contentType)
    Source.futureSource(
      Future(contentEncoding.decode[T](element)).map { req =>
        val source = Source
          .single(req)
          .flatMapConcat(handler.handle)
          .recover(handler.errorEncoder)

        withMetrics(source, req, metricsEnabled, gauge, hostAddress)
      }
    )
  }

}
