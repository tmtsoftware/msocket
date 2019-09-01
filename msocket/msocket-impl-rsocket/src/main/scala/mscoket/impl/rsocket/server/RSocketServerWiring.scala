package mscoket.impl.rsocket.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.Decoder
import io.rsocket.transport.akka.server.WebsocketServerTransport
import io.rsocket.{Payload, RSocket}
import msocket.api.RequestHandler
import reactor.core.publisher.Mono

import scala.concurrent.ExecutionContext

class RSocketServerWiring[Req: Decoder](requestHandler: RequestHandler[Req, Source[Payload, NotUsed]], interface: String, port: Int)(
    implicit actorSystem: ActorSystem
) {
  implicit val mat: Materializer    = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val transport           = new WebsocketServerTransport(interface, port)
  private val socket: RSocket     = new RSocketImpl(requestHandler)
  val serverHelper: RSocketServer = new RSocketServer(transport, (_, _) => Mono.just(socket))
}
