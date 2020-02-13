package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import io.rsocket.transport.netty.server.WebsocketServerTransport
import io.rsocket.{RSocket, RSocketFactory}
import msocket.api.ContentType
import reactor.core.publisher.Mono

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.ExecutionContext

class RSocketServer(rSocketF: ContentType => RSocket)(implicit actorSystem: ActorSystem[_]) {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  def start(interface: String, port: Int): Unit = {
    val transport = WebsocketServerTransport.create(interface, port)

    RSocketFactory.receive
      .acceptor { (setupPayload, _) =>
        val contentType = ContentType.fromMimeType(setupPayload.dataMimeType())
        Mono.just(rSocketF(contentType))
      }
      .transport(transport)
      .start
      .toFuture
      .toScala
      .onComplete(println)
  }
}
