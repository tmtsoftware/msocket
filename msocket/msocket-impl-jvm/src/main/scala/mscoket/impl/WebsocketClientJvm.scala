package mscoket.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import mscoket.impl.Encoding.JsonText
import msocket.api.Result.{Error, Success}
import msocket.api.{Result, RequestClient}

import scala.concurrent.Future

class WebsocketClientJvm[Req: Encoder](baseUri: String)(implicit actorSystem: ActorSystem) extends RequestClient[Req] {

  implicit lazy val matL: Materializer = ActorMaterializer()

  private val setup = new WebsocketClientSetup(WebSocketRequest(baseUri))

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    setup
      .request(JsonText.strictMessage(request))
      .collect {
        case TextMessage.Strict(text) => JsonText.decodeText(text)
      }
  }

  def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
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

  def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }
}
