package msocket.impl.rsocket.client

import io.rsocket.transport.ClientTransport
import io.rsocket.{RSocket, RSocketFactory}

import scala.concurrent.Future
import scala.compat.java8.FutureConverters.CompletionStageOps

object RSocketFactoryS {
  def client(clientTransport: ClientTransport): Future[RSocket] = {
    RSocketFactory.connect
      .frameDecoder(_.retain)
      .transport(clientTransport)
      .start
      .toFuture
      .toScala
  }
}
