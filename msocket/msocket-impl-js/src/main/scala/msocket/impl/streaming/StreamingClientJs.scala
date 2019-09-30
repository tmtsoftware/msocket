package msocket.impl.streaming

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{StreamStatus, Transport}

import scala.concurrent.{Future, Promise}

abstract class StreamingClientJs[Req: Encoder](connectionFactory: ConnectionFactory[Req]) extends Transport[Req] {

  override def requestResponseWithDelay[Res: Decoder](request: Req): Future[Res] = {
    val promise: Promise[Res] = Promise()
    val connectedSource       = connectionFactory.connect(request, new PlainConnectedSource)
    connectedSource.onMessage = { response =>
      promise.trySuccess(response)
      connectedSource.disconnect()
    }
    promise.future
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    connectionFactory.connect(request, new PlainConnectedSource)
  }

  override def requestStreamWithError[Res: Decoder](request: Req): Source[Res, Future[StreamStatus]] = {
    connectionFactory.connect(request, new ConnectedSourceWithErr)
  }
}
