package msocket.simple.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.StandardRoute
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import csw.simple.api.{Codecs, PostRequest, SimpleApi, StreamRequest}
import csw.simple.impl.SimpleImpl
import mscoket.impl.RoutesFactory
import msocket.api.RequestHandler

import scala.concurrent.ExecutionContext

class Wiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val simpleImpl: SimpleApi                                                     = new SimpleImpl
  lazy val websocketHandler: RequestHandler[StreamRequest, Source[Message, NotUsed]] = new SimpleWebsocketHandler(simpleImpl)
  lazy val postHandler: RequestHandler[PostRequest, StandardRoute]                   = new SimplePostHandler(simpleImpl)
  lazy val routesFactory                                                             = new RoutesFactory(postHandler, websocketHandler)
  lazy val simpleServer                                                              = new SimpleServer(routesFactory.route)
}
