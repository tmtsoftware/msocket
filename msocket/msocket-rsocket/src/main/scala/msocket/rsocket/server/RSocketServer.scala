package msocket.rsocket.server

import org.apache.pekko.actor.typed.ActorSystem
import io.rsocket.RSocket
import io.rsocket.core
import io.rsocket.transport.netty.server.{CloseableChannel, WebsocketServerTransport}
import msocket.api.ContentType
import reactor.core.publisher.Mono

import scala.jdk.FutureConverters.*
import scala.concurrent.{ExecutionContext, Future}

class RSocketServer(rSocketF: ContentType => RSocket)(implicit actorSystem: ActorSystem[?]) {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  @volatile
  private var channelF: Future[CloseableChannel] = scala.compiletime.uninitialized

  def start(interface: String, port: Int): Future[CloseableChannel] = {
    val transport = WebsocketServerTransport.create(interface, port)

    val eventualChannel = core.RSocketServer
      .create { (setupPayload, _) =>
        val contentType = ContentType.fromMimeType(setupPayload.dataMimeType())
        Mono.just(rSocketF(contentType))
      }
      .bind(transport)
      .toFuture
      .asScala

    channelF = eventualChannel

    eventualChannel
  }

  def stop(): Future[Unit] = {
    channelF.map(_.dispose())
  }
}
