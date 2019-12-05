package msocket.impl.rsocket.client

import akka.actor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.Materializer
import io.bullet.borer.Encoder
import io.rsocket.RSocket
import io.rsocket.transport.akka.client.WebsocketClientTransport
import msocket.api.{ErrorProtocol, Transport}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, ExecutionContext, Future}

class RSocketTransportFactory[Req: Encoder: ErrorProtocol](implicit actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext       = actorSystem.executionContext
  private implicit val system: actor.ActorSystem  = actorSystem.toClassic
  private implicit val materializer: Materializer = Materializer(actorSystem)

  def transport(uri: String): Transport[Req] = {
    val transport                       = new WebsocketClientTransport(WebSocketRequest.fromTargetUriString(uri))
    val eventualSocket: Future[RSocket] = RSocketFactoryS.client(transport)
    Await.result(eventualSocket.map(x => new RSocketTransport(x)), 5.seconds)
  }
}
