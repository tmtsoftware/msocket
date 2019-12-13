package msocket.example.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.{Codecs, ExampleRequest}
import csw.example.impl.ExampleImpl
import io.rsocket.Payload
import msocket.api.{Encoding, MessageHandler}
import msocket.example.server.handlers.{ExamplePostHandler, ExampleRSocketHandler, ExampleSseHandler, ExampleWebsocketHandler}
import msocket.impl.RouteFactory
import msocket.impl.post.PostRouteFactory
import msocket.impl.rsocket.server.RSocketServer
import msocket.impl.sse.SseRouteFactory
import msocket.impl.ws.WebsocketRouteFactory

import scala.concurrent.ExecutionContext

class ServerWiring extends Codecs {
  implicit lazy val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "server")
  implicit lazy val ec: ExecutionContext        = actorSystem.executionContext

  lazy val exampleImpl: ExampleApi = new ExampleImpl

//  lazy val locationService: LocationService       = HttpLocationServiceFactory.makeLocalClient(actorSystem)
//  lazy val securityDirectives: SecurityDirectives = SecurityDirectives(locationService)

  lazy val postHandler: MessageHandler[ExampleRequest, Route] = new ExamplePostHandler(exampleImpl)

  lazy val sseHandler: MessageHandler[ExampleRequest, Route] = new ExampleSseHandler(exampleImpl)

  def websocketHandlerFactory(encoding: Encoding[_]): MessageHandler[ExampleRequest, Source[Message, NotUsed]] =
    new ExampleWebsocketHandler(exampleImpl, encoding)

  lazy val rSocketHandler: MessageHandler[ExampleRequest, Source[Payload, NotUsed]] = new ExampleRSocketHandler(exampleImpl)

  lazy val applicationRoute: Route = RouteFactory.combine(
    new PostRouteFactory[ExampleRequest]("post-endpoint", postHandler),
    new WebsocketRouteFactory[ExampleRequest]("websocket-endpoint", websocketHandlerFactory),
    new SseRouteFactory[ExampleRequest]("sse-endpoint", sseHandler)
  )

  lazy val exampleServer = new ExampleServer(applicationRoute)
  lazy val rSocketServer = new RSocketServer(rSocketHandler)
}
