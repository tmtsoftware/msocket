package msocket.simple.server

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import csw.simple.api.Codecs
import csw.simple.impl.SimpleImpl
import msocket.core.server.WsServerFlow

import scala.concurrent.ExecutionContext

class Wiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val simpleImpl   = new SimpleImpl
  lazy val socket       = new SimpleTextSocket(simpleImpl)
  lazy val simpleServer = new SimpleServer(new WsServerFlow(socket))
}
