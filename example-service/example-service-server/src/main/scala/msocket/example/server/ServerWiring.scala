package msocket.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import csw.example.impl.ExampleImpl
import msocket.api.Encoding
import msocket.example.server.handlers._
import msocket.impl.RouteFactory
import msocket.impl.post.PostRouteFactory
import msocket.impl.rsocket.server.RSocketServer
import msocket.impl.sse.SseRouteFactory
import msocket.impl.ws.WebsocketRouteFactory

import scala.concurrent.ExecutionContext

class ServerWiring extends ExampleCodecs {
  implicit lazy val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "server")
  implicit lazy val ec: ExecutionContext        = actorSystem.executionContext

  lazy val exampleImpl: ExampleApi = new ExampleImpl

//  lazy val locationService: LocationService       = HttpLocationServiceFactory.makeLocalClient(actorSystem)
//  lazy val securityDirectives: SecurityDirectives = SecurityDirectives(locationService)

  lazy val postHandler: ExamplePostStreamingHandler                           = new ExamplePostStreamingHandler(exampleImpl)
  lazy val sseHandler: ExampleSseHandler                                      = new ExampleSseHandler(exampleImpl)
  def websocketHandlerFactory(encoding: Encoding[_]): ExampleWebsocketHandler = new ExampleWebsocketHandler(exampleImpl, encoding)
  lazy val requestResponseHandler: ExampleRSocketResponseHandler              = new ExampleRSocketResponseHandler(exampleImpl)
  lazy val requestStreamHandler: ExampleRSocketStreamHandler                  = new ExampleRSocketStreamHandler(exampleImpl)

  lazy val applicationRoute: Route = RouteFactory.combine(
    new PostRouteFactory[ExampleRequest]("post-endpoint", postHandler),
    new WebsocketRouteFactory[ExampleRequest]("websocket-endpoint", websocketHandlerFactory),
    new SseRouteFactory[ExampleRequest]("sse-endpoint", sseHandler)
  )

  lazy val exampleServer = new ExampleServer(applicationRoute)
  lazy val rSocketServer = new RSocketServer(requestResponseHandler, requestStreamHandler)
}
