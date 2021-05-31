package msocket.http.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Flow, Source}
import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentEncoding, ErrorProtocol}
import msocket.http.CborByteString
import msocket.jvm.metrics.{LabelExtractor, MetricCollector}
import msocket.jvm.stream.StreamRequestHandler
import msocket.security.AccessController

import scala.concurrent.duration.DurationLong
import scala.util.{Failure, Success}

class WebsocketServerFlow[Req: Decoder: ErrorProtocol: LabelExtractor](
    streamRequestHandler: StreamRequestHandler[Req],
    collectorFactory: Req => MetricCollector[Req],
    accessController: AccessController
)(implicit actorSystem: ActorSystem[_]) {

  val flow: Flow[Message, Message, NotUsed] = {
    Flow[Message]
      .via(cleanup)
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
    val wsResponseEncoder = new WebsocketStreamResponseEncoder[Req](contentEncoding.contentType, accessController)
    Source
      .lazySingle(() => contentEncoding.decode[Req](element))
      .flatMapConcat(req => wsResponseEncoder.encodeStream(streamRequestHandler.handle(req), collectorFactory(req)))
      .via(cleanup)
      .recover(wsResponseEncoder.errorEncoder)
  }

  private lazy val cleanupSwitch = KillSwitches.shared("websocket-cleanup-switch")

  private def cleanup[T]: Flow[T, T, NotUsed] = Flow[T]
    .via(cleanupSwitch.flow)
    .watchTermination() { case (mat, doneF) =>
      import actorSystem.executionContext
      doneF.onComplete {
        case Success(_)  => cleanupSwitch.shutdown()
        case Failure(ex) => cleanupSwitch.abort(ex)
      }
      mat
    }
}
