package msocket.impl.rsocket.client

import java.net.URI

import akka.actor.typed.ActorSystem
import io.bullet.borer.Encoder
import io.rsocket.RSocket
import io.rsocket.transport.netty.client.WebsocketClientTransport
import msocket.api.{ContentType, ErrorProtocol, Transport}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, ExecutionContext, Future}

class RSocketTransportFactory[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext = actorSystem.executionContext

  def connect(uri: String, contentType: ContentType): Transport[Req] = {
    val transport                       = WebsocketClientTransport.create(URI.create(uri))
    val eventualSocket: Future[RSocket] = RSocketFactoryS.client(transport, contentType)
    Await.result(eventualSocket.map(rSocket => new RSocketTransport(rSocket, contentType)), 5.seconds)
  }
}
