package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import io.bullet.borer.Decoder
import io.rsocket.transport.netty.server.WebsocketServerTransport
import io.rsocket.{RSocket, RSocketFactory}
import msocket.api.ErrorProtocol
import reactor.core.publisher.Mono

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.ExecutionContext

class RSocketServer[Req: Decoder: ErrorProtocol](
    requestResponseHandler: RSocketResponseHandler[Req],
    requestStreamHandler: RSocketStreamHandler[Req]
)(implicit actorSystem: ActorSystem[_]) {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  def start(interface: String, port: Int): Unit = {
    val socket: RSocket = new RSocketImpl(requestResponseHandler, requestStreamHandler)
    val transport       = WebsocketServerTransport.create(interface, port)

    RSocketFactory.receive
      .acceptor((_, _) => Mono.just(socket))
      .transport(transport)
      .start
      .toFuture
      .toScala
      .onComplete(println)
  }
}
