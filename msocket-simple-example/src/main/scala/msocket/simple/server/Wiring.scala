package msocket.simple.server

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import csw.simple.api.Codecs
import csw.simple.impl.SimpleImpl
import msocket.core.api.Encoding
import msocket.core.api.Encoding.{JsonBinary, JsonText}
import msocket.core.server.WsServerFlow

import scala.concurrent.ExecutionContext

class Wiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher
  implicit lazy val mat: Materializer        = ActorMaterializer()
  implicit lazy val encoding: Encoding       = JsonText

  lazy val simpleImpl   = new SimpleImpl
  lazy val socket       = new SimpleServerSocket(simpleImpl)
  lazy val simpleServer = new SimpleServer(new WsServerFlow(socket))
}
