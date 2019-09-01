package mscoket.impl.rsocket.server

import io.rsocket.transport.ServerTransport
import io.rsocket.{Closeable, RSocketFactory, SocketAcceptor}

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.Future

class RSocketServer(serverTransport: ServerTransport[_ <: Closeable], socketAcceptor: SocketAcceptor) {
  def start: Future[Closeable] = {
    RSocketFactory.receive
      .frameDecoder(_.retain)
      .acceptor(socketAcceptor)
      .transport(serverTransport)
      .start
      .toFuture
      .toScala
  }
}
