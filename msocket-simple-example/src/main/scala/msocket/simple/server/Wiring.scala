package msocket.simple.server

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import csw.simple.api.Protocol._
import csw.simple.api.{Codecs, Protocol}
import csw.simple.impl.SimpleImpl
import msocket.core.server.{ServerHandler, WsServerFlow}

import scala.concurrent.ExecutionContext

class Wiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val simpleImpl                                                       = new SimpleImpl
  lazy val socket                                                           = new SimpleTextSocket(simpleImpl)
  lazy val handler: ServerHandler[Protocol, RequestResponse, RequestStream] = new ServerHandler(socket)
  lazy val serverFlow                                                       = new WsServerFlow(handler)
  lazy val simpleServer                                                     = new SimpleServer(serverFlow)
}
