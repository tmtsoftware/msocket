package msocket.impl.rsocket.client

import java.net.URI

import akka.actor.typed.ActorSystem
import io.bullet.borer.Encoder
import io.rsocket.RSocket
import io.rsocket.transport.netty.client.WebsocketClientTransport
import msocket.api.{Encoding, ErrorProtocol, Transport}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, ExecutionContext, Future}

class RSocketTransportFactory[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext = actorSystem.executionContext

  def transport(uri: String, encoding: Encoding[_]): Transport[Req] = {
    val transport                       = WebsocketClientTransport.create(URI.create(uri))
    val eventualSocket: Future[RSocket] = RSocketFactoryS.client(transport, encoding)
    Await.result(eventualSocket.map(rSocket => new RSocketTransport(rSocket, encoding)), 5.seconds)
  }
}
