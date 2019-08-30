package msocket.impl.streaming

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.RequestClient

import scala.concurrent.{Future, Promise}

class StreamingClientJs[Req: Encoder](connectionFactory: ConnectionFactory[Req]) extends RequestClient[Req] {
  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    connectionFactory.connect(request, new SimpleConnectedSource)
  }

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    connectionFactory.connect(request, new ConnectedSourceWithErr)
  }

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    val promise: Promise[Res] = Promise()
    val connectedSource       = connectionFactory.connect(request, new SimpleConnectedSource)
    connectedSource.onMessage = { response =>
      promise.trySuccess(response)
      connectedSource.disconnect()
    }
    promise.future
  }
}
