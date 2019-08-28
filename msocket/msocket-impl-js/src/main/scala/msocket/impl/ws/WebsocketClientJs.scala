package msocket.impl.ws

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.RequestClient

import scala.concurrent.{ExecutionContext, Future, Promise}

class WebsocketClientJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext) extends RequestClient[Req] {
  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    trigger(new SimpleWebsocketSource(uri))(request)
  }

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    trigger(new WebsocketSourceWithErr[Req, Res, Err](uri))(request)
  }

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    val promise: Promise[Res] = Promise()
    val websocketSource       = trigger(new SimpleWebsocketSource(uri))(request)
    websocketSource.onMessage = { response =>
      promise.trySuccess(response)
      websocketSource.close()
    }
    promise.future
  }

  private def trigger[A <: WebsocketSource[Req, _, _]](websocketSource: A)(request: Req): A = {
    websocketSource.send(request)
    websocketSource
  }
}
