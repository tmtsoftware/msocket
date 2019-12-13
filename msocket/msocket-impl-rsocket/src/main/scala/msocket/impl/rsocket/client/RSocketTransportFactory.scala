package msocket.impl.rsocket.client

import java.net.URI

import akka.actor.typed.ActorSystem
import io.bullet.borer.Encoder
import io.rsocket.RSocket
import io.rsocket.transport.netty.client.WebsocketClientTransport
import msocket.api.{ErrorProtocol, Transport}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, ExecutionContext, Future}

class RSocketTransportFactory[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext = actorSystem.executionContext

  def transport(uri: String): Transport[Req] = {
    val transport                       = WebsocketClientTransport.create(URI.create(uri))
    val eventualSocket: Future[RSocket] = RSocketFactoryS.client(transport)
    Await.result(eventualSocket.map(x => new RSocketTransport(x)), 5.seconds)
  }
}
