package msocket.example.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import csw.aas.http.SecurityDirectives
import csw.example.api.ExampleApi
import csw.example.api.protocol.{Codecs, ExampleRequest}
import csw.example.impl.ExampleImpl
import csw.location.api.scaladsl.LocationService
import csw.location.client.scaladsl.HttpLocationServiceFactory
import io.rsocket.Payload
import mscoket.impl.RoutesFactory
import mscoket.impl.rsocket.server.RSocketServer
import mscoket.impl.ws.Encoding
import msocket.api.MessageHandler
import msocket.example.server.handlers.{ExamplePostHandler, ExampleRSocketHandler, ExampleSseHandler, ExampleWebsocketHandler}

import scala.concurrent.ExecutionContext

class ServerWiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("server")
  implicit lazy val ec: ExecutionContext     = actorSystem.dispatcher

  lazy val exampleImpl: ExampleApi = new ExampleImpl

  lazy val locationService: LocationService = HttpLocationServiceFactory.makeLocalClient(actorSystem.toTyped, implicitly)
  lazy val securityDirectives               = SecurityDirectives(locationService)

  lazy val postHandler: MessageHandler[ExampleRequest, Route] = new ExamplePostHandler(exampleImpl, securityDirectives)

  lazy val sseHandler: MessageHandler[ExampleRequest, Route] = new ExampleSseHandler(exampleImpl)

  def websocketHandlerFactory(encoding: Encoding[_]): MessageHandler[ExampleRequest, Source[Message, NotUsed]] =
    new ExampleWebsocketHandler(exampleImpl, encoding)

  lazy val rSocketHandler: MessageHandler[ExampleRequest, Source[Payload, NotUsed]] = new ExampleRSocketHandler(exampleImpl)

  lazy val routesFactory = new RoutesFactory(postHandler, websocketHandlerFactory, sseHandler)
  lazy val exampleServer = new ExampleServer(routesFactory.route)
  lazy val rSocketServer = new RSocketServer(rSocketHandler)
}
