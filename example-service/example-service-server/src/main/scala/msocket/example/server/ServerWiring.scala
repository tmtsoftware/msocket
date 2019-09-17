package msocket.example.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import csw.aas.http.SecurityDirectives
import csw.example.api.ExampleApi
import csw.example.api.protocol.{Codecs, ExampleRequest}
import csw.example.impl.ExampleImpl
import csw.location.client.scaladsl.HttpLocationServiceFactory
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

  lazy val locationService = HttpLocationServiceFactory.makeLocalClient(actorSystem.toTyped, mat)
  lazy val securityDirectives = SecurityDirectives(locationService)

  lazy val postHandler: RequestHandler[ExampleRequest, Route] = new ExamplePostHandler(exampleImpl, securityDirectives)

  lazy val sseHandler: RequestHandler[ExampleRequest, Route] = new ExampleSseHandler(exampleImpl)

  lazy val websocketHandler: RequestHandler[ExampleRequest, Source[Message, NotUsed]] = new ExampleWebsocketHandler(exampleImpl)

  lazy val rSocketHandler: RequestHandler[ExampleRequest, Source[Payload, NotUsed]] = new ExampleRSocketHandler(exampleImpl)

  lazy val routesFactory = new RoutesFactory(postHandler, websocketHandler, sseHandler)
  lazy val exampleServer = new ExampleServer(routesFactory.route)
  lazy val rSocketServer = new RSocketServer(rSocketHandler)
}
