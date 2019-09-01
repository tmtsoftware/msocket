package msocket.example.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.StandardRoute
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import csw.example.api.ExampleApi
import csw.example.api.protocol.{Codecs, ExampleRequest}
import csw.example.impl.ExampleImpl
import io.rsocket.Payload
import mscoket.impl.RoutesFactory
import mscoket.impl.rsocket.server.RSocketServer
import msocket.api.RequestHandler
import msocket.example.server.handlers.{ExamplePostHandler, ExampleRSocketHandler, ExampleSseHandler, ExampleWebsocketHandler}

import scala.concurrent.ExecutionContext

class ServerWiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val exampleImpl: ExampleApi = new ExampleImpl

  lazy val postHandler: RequestHandler[ExampleRequest, StandardRoute]                 = new ExamplePostHandler(exampleImpl)
  lazy val sseHandler: RequestHandler[ExampleRequest, StandardRoute]                  = new ExampleSseHandler(exampleImpl)
  lazy val websocketHandler: RequestHandler[ExampleRequest, Source[Message, NotUsed]] = new ExampleWebsocketHandler(exampleImpl)
  lazy val rSocketHandler: RequestHandler[ExampleRequest, Source[Payload, NotUsed]]   = new ExampleRSocketHandler(exampleImpl)

  lazy val routesFactory = new RoutesFactory(postHandler, websocketHandler, sseHandler)
  lazy val exampleServer = new ExampleServer(routesFactory.route)
  lazy val rSocketServer = new RSocketServer(rSocketHandler)
}
