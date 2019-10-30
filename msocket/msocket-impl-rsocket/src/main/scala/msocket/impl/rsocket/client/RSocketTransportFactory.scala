package msocket.impl.rsocket.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.WebSocketRequest
import io.bullet.borer.Encoder
import io.rsocket.RSocket
import io.rsocket.transport.akka.client.WebsocketClientTransport
import msocket.api.Transport

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, ExecutionContext, Future}

class RSocketTransportFactory[Req: Encoder](implicit actorSystem: ActorSystem) {
  private implicit val ec: ExecutionContext = actorSystem.dispatcher

  def transport(uri: String): Transport[Req] = {
    val transport                       = new WebsocketClientTransport(WebSocketRequest.fromTargetUriString(uri))
    val eventualSocket: Future[RSocket] = RSocketFactoryS.client(transport)
    Await.result(eventualSocket.map(x => new RSocketTransport(x)), 5.seconds)
  }
}