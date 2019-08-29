package msocket.impl.streaming

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.RequestClient

import scala.concurrent.{Future, Promise}

class StreamingClientJs[Req: Encoder](connectionFactory: Connection[Req]) extends RequestClient[Req] {
  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    new SimpleConnectedSource[Res].connect(connectionFactory, request)
  }

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    new ConnectedSourceWithErr[Res, Err].connect(connectionFactory, request)
  }

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    val promise: Promise[Res] = Promise()
    val connectedSource       = new SimpleConnectedSource[Res].connect(connectionFactory, request)
    connectedSource.onMessage = { response =>
      promise.trySuccess(response)
      connectedSource.disconnect()
    }
    promise.future
  }
}
