package mscoket.impl.rsocket.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.Encoder
import io.rsocket.RSocket
import io.rsocket.transport.akka.client.WebsocketClientTransport
import msocket.api.RequestClient
import concurrent.duration.DurationLong

import scala.concurrent.{Await, ExecutionContext, Future}

class RSocketClientFactory[Req: Encoder](implicit actorSystem: ActorSystem) {
  private implicit val mat: Materializer    = ActorMaterializer()
  private implicit val ec: ExecutionContext = actorSystem.dispatcher

  def client(uri: String): RequestClient[Req] = {
    val transport                       = new WebsocketClientTransport(WebSocketRequest.fromTargetUriString(uri))
    val eventualSocket: Future[RSocket] = RSocketFactoryS.client(transport)
    Await.result(eventualSocket.map(x => new RSocketClient(x)), 5.seconds)
  }
}
