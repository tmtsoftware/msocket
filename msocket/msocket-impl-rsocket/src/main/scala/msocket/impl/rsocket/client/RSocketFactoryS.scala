package msocket.impl.rsocket.client

import io.rsocket.transport.ClientTransport
import io.rsocket.{RSocket, RSocketFactory}
import msocket.api.Encoding

import scala.concurrent.Future
import scala.compat.java8.FutureConverters.CompletionStageOps

object RSocketFactoryS {
  def client(clientTransport: ClientTransport, encoding: Encoding[_]): Future[RSocket] = {
    RSocketFactory.connect
      .dataMimeType(encoding.mimeType)
      .transport(clientTransport)
      .start
      .toFuture
      .toScala
  }
}
