package msocket.impl.rsocket.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import io.bullet.borer.Decoder
import io.rsocket.transport.netty.server.WebsocketServerTransport
import io.rsocket.{Payload, RSocket, RSocketFactory}
import msocket.api.{ErrorProtocol, MessageHandler}
import reactor.core.publisher.Mono

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}

class RSocketServer[Req: Decoder: ErrorProtocol](
    requestResponseHandler: MessageHandler[Req, Future[Payload]],
    requestStreamHandler: MessageHandler[Req, Source[Payload, NotUsed]]
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
