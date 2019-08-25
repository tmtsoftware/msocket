package mscoket.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{WebsocketClient, Payload, Result}
import msocket.api.Result.{Error, Success}

import scala.concurrent.Future

class WebsocketClientImpl[Req: Encoder](baseUri: String, encoding: Encoding)(implicit actorSystem: ActorSystem)
    extends WebsocketClient[Req] {

  implicit lazy val matL: Materializer = ActorMaterializer()

  private val setup = new WebsocketClientSetup(WebSocketRequest(s"$baseUri/${encoding.Name}"))

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, NotUsed] = {
    setup
      .request(encoding.strictMessage(Payload(request)))
      .collect {
        case BinaryMessage.Strict(data) if !encoding.isBinary => encoding.decodeBinary(data).value
        case TextMessage.Strict(text) if !encoding.isBinary   => encoding.decodeText(text).value
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
