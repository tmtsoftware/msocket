package mscoket.impl.rsocket.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.Encoder
import io.rsocket.RSocket
import io.rsocket.transport.akka.client.WebsocketClientTransport
import msocket.api.RequestClient

import scala.concurrent.{ExecutionContext, Future}

class RSocketClientWiring[Req: Encoder](uri: String)(implicit actorSystem: ActorSystem) {
  implicit val mat: Materializer    = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val transport                           = new WebsocketClientTransport(WebSocketRequest.fromTargetUriString(uri))
  val eventualSocket: Future[RSocket]     = RSocketFactoryS.client(transport)
  val clientF: Future[RequestClient[Req]] = eventualSocket.map(x => new RSocketClient(x))
}
