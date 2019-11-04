package msocket.impl.rsocket.server

import akka.{NotUsed, actor}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.bullet.borer.Decoder
import io.rsocket.transport.akka.server.WebsocketServerTransport
import io.rsocket.{Payload, RSocket, RSocketFactory}
import msocket.api.MessageHandler
import reactor.core.publisher.Mono

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.ExecutionContext

class RSocketServer[Req: Decoder](requestHandler: MessageHandler[Req, Source[Payload, NotUsed]])(implicit actorSystem: ActorSystem[_]) {

  implicit val ec: ExecutionContext               = actorSystem.executionContext
  implicit val system: actor.ActorSystem          = actorSystem.toClassic
  private implicit val materializer: Materializer = Materializer(actorSystem)

  def start(interface: String, port: Int): Unit = {
    val socket: RSocket = new RSocketImpl(requestHandler)
    val transport       = new WebsocketServerTransport(interface, port)

    RSocketFactory.receive
      .frameDecoder(_.retain)
      .acceptor((_, _) => Mono.just(socket))
      .transport(transport)
      .start
      .toFuture
      .toScala
      .onComplete(println)
  }
}
