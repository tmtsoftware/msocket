package mscoket.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import mscoket.impl.Encoding.JsonText
import msocket.api.Result.{Error, Success}
import msocket.api.{Payload, Result, WebsocketClient}

import scala.concurrent.Future

class WebsocketClientJvm[Req: Encoder](baseUri: String)(implicit actorSystem: ActorSystem) extends WebsocketClient[Req] {

  implicit lazy val matL: Materializer = ActorMaterializer()

  private val setup = new WebsocketClientSetup(WebSocketRequest(baseUri))

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, NotUsed] = {
    setup
      .request(JsonText.strictMessage(Payload(request)))
      .collect {
        case TextMessage.Strict(text) => JsonText.decodeText(text).value
      }
  }

  def requestStreamWithError[Res: Decoder: Encoder, Err: Decoder: Encoder](request: Req): Source[Res, Future[Option[Err]]] = {
    val streamOfStreams = requestStream[Result[Res, Err]](request).prefixAndTail(1).map {
      case (xs, stream) =>
        xs.toList match {
          case Error(e) :: _   => Source.empty.mapMaterializedValue(_ => Some(e))
          case Success(r) :: _ => Source.single(r).concat(stream.collect { case Success(r) => r }).mapMaterializedValue(_ => None)
          case Nil             => Source.empty.mapMaterializedValue(_ => None)
        }
    }
    Source.fromFutureSource(streamOfStreams.runWith(Sink.head))
  }

  def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }
}
