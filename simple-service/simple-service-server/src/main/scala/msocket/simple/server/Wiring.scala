package msocket.simple.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.StandardRoute
import akka.stream.{ActorMaterializer, Materializer}
import csw.simple.api.{Codecs, PostRequest, SimpleApi, WebsocketRequest}
import csw.simple.impl.SimpleImpl
import mscoket.impl.RoutesFactory
import msocket.api.{PostHandler, WebsocketHandler}

import scala.concurrent.ExecutionContext

class Wiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val simpleImpl: SimpleApi                                = new SimpleImpl
  lazy val websocketHandler: WebsocketHandler[WebsocketRequest] = new SimpleWebsocketHandler(simpleImpl)
  lazy val postHandler: PostHandler[PostRequest, StandardRoute] = new SimplePostHandler(simpleImpl)
  lazy val routesFactory                                        = new RoutesFactory(postHandler, websocketHandler)
  lazy val simpleServer                                         = new SimpleServer(routesFactory.route)
}
