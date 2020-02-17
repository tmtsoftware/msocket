package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import io.rsocket.transport.netty.server.{CloseableChannel, WebsocketServerTransport}
import io.rsocket.{RSocket, RSocketFactory}
import msocket.api.ContentType
import reactor.core.publisher.Mono

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}

class RSocketServer(rSocketF: ContentType => RSocket)(implicit actorSystem: ActorSystem[_]) {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  @volatile
  private var channelF: Future[CloseableChannel] = _

  def start(interface: String, port: Int): Future[CloseableChannel] = {
    val transport = WebsocketServerTransport.create(interface, port)

    val eventualChannel = RSocketFactory.receive
      .acceptor { (setupPayload, _) =>
        val contentType = ContentType.fromMimeType(setupPayload.dataMimeType())
        Mono.just(rSocketF(contentType))
      }
      .transport(transport)
      .start
      .toFuture
      .toScala

    channelF = eventualChannel

    eventualChannel
  }

  def stop(): Unit = {
    channelF.foreach(_.dispose())
  }
}
