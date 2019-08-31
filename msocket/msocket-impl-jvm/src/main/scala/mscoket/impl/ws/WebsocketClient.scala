package mscoket.impl.ws

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import mscoket.impl.StreamSplitter._
import mscoket.impl.ws.Encoding.JsonText
import msocket.api.{RequestClient, Result}

import scala.concurrent.{ExecutionContext, Future}

class WebsocketClient[Req: Encoder](uri: String)(implicit actorSystem: ActorSystem) extends RequestClient[Req] {

  implicit lazy val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext   = actorSystem.dispatcher

  private val setup = new WebsocketClientSetup(WebSocketRequest(uri))

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }

  override def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    setup
      .request(JsonText.strictMessage(request))
      .collect {
        case TextMessage.Strict(text) => JsonText.decodeText(text)
      }
  }

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    requestStream[Result[Res, Err]](request).split
  }

}
