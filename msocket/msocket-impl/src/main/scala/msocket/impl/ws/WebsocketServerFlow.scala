package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentEncoding, ErrorProtocol, Labelled, StreamRequestHandler}
import msocket.impl.CborByteString
import msocket.impl.metrics.MetricCollector

import scala.concurrent.duration.DurationLong

class WebsocketServerFlow[Req: Decoder: ErrorProtocol: Labelled](
    streamRequestHandler: StreamRequestHandler[Req],
    collectorFactory: Req => MetricCollector[Req]
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

  private def handle[Elm](element: Elm, contentEncoding: ContentEncoding[Elm]): Source[Message, NotUsed] = {
    val wsHandler = new WebsocketHandler[Req](contentEncoding.contentType)
    Source
      .lazySingle(() => contentEncoding.decode[Req](element))
      .flatMapConcat(req => wsHandler.handle(streamRequestHandler.handle(req), collectorFactory(req)))
      .recover(wsHandler.errorEncoder)
  }

}
