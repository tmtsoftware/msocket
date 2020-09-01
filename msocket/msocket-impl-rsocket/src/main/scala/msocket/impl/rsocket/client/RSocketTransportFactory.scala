package msocket.impl.rsocket.client

import java.net.URI

import akka.actor.typed.ActorSystem
import io.bullet.borer.Encoder
import io.rsocket.transport.netty.client.WebsocketClientTransport
import msocket.api.{ContentType, ErrorProtocol, Subscription}

import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

class RSocketTransportFactory {
  def connect[Req: Encoder: ErrorProtocol](uri: String, contentType: ContentType)(implicit
      actorSystem: ActorSystem[_]
  ): (RSocketTransport[Req], Subscription) = {
    val underlyingTransport = WebsocketClientTransport.create(URI.create(uri))
    val rSocket             = Await.result(RSocketFactoryS.client(underlyingTransport, contentType), 5.seconds)
    val transport           = new RSocketTransport(rSocket, contentType)
    (transport, transport.subscription())
  }
}

object RSocketTransportFactory extends RSocketTransportFactory
