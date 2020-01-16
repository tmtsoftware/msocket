package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import io.bullet.borer.Decoder
import io.rsocket.RSocketFactory
import io.rsocket.transport.netty.server.WebsocketServerTransport
import msocket.api.{ContentType, ErrorProtocol}
import reactor.core.publisher.Mono

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.ExecutionContext

class RSocketServer[Req: Decoder: ErrorProtocol](
    requestResponseHandler: ContentType => RSocketResponseHandler[Req],
    requestStreamHandler: ContentType => RSocketStreamHandler[Req]
)(implicit actorSystem: ActorSystem[_]) {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  def start(interface: String, port: Int): Unit = {
    val transport = WebsocketServerTransport.create(interface, port)

    RSocketFactory.receive
      .acceptor { (setupPayload, _) =>
        val contentType = ContentType.fromMimeType(setupPayload.dataMimeType())
        Mono.just(new RSocketImpl(requestResponseHandler(contentType), requestStreamHandler(contentType), contentType))
      }
      .transport(transport)
      .start
      .toFuture
      .toScala
      .onComplete(println)
  }
}
