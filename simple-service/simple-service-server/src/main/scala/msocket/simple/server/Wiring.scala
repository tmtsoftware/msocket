package msocket.simple.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.StandardRoute
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import csw.simple.api.{Codecs, SimpleApi, SimpleRequest}
import csw.simple.impl.SimpleImpl
import mscoket.impl.RoutesFactory
import msocket.api.RequestHandler
import msocket.simple.server.handlers.{SimplePostHandler, SimpleSseHandler, SimpleWebsocketHandler}

import scala.concurrent.ExecutionContext

class Wiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val simpleImpl: SimpleApi                                                     = new SimpleImpl
  lazy val websocketHandler: RequestHandler[SimpleRequest, Source[Message, NotUsed]] = new SimpleWebsocketHandler(simpleImpl)
  lazy val sseHandler: RequestHandler[SimpleRequest, StandardRoute]                  = new SimpleSseHandler(simpleImpl)
  lazy val postHandler: RequestHandler[SimpleRequest, StandardRoute]                 = new SimplePostHandler(simpleImpl)
  lazy val routesFactory                                                             = new RoutesFactory(postHandler, websocketHandler, sseHandler)
  lazy val simpleServer                                                              = new SimpleServer(routesFactory.route)
}
