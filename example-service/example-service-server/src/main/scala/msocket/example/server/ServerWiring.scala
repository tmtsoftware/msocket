package msocket.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import csw.example.impl.ExampleImpl
import msocket.api.ContentType
import msocket.example.server.handlers._
import msocket.impl.RouteFactory
import msocket.impl.post.PostRouteFactory
import msocket.impl.rsocket.server.RSocketServer
import msocket.impl.sse.SseRouteFactory
import msocket.impl.ws.WebsocketRouteFactory

import scala.concurrent.ExecutionContext

/** This is where the supported handlers are wired with the server */
class ServerWiring extends ExampleCodecs {
  implicit lazy val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "server")
  implicit lazy val ec: ExecutionContext        = actorSystem.executionContext

  lazy val exampleImpl: ExampleApi = new ExampleImpl

  lazy val postHandler: ExamplePostStreamingHandler                       = new ExamplePostStreamingHandler(exampleImpl)
  lazy val sseHandler: ExampleSseHandler                                  = new ExampleSseHandler(exampleImpl)
  def websocketHandler(contentType: ContentType): ExampleWebsocketHandler = new ExampleWebsocketHandler(exampleImpl, contentType)
  def requestResponseHandler(contentType: ContentType): ExampleRSocketResponseHandler =
    new ExampleRSocketResponseHandler(exampleImpl, contentType)
  def requestStreamHandler(contentType: ContentType): ExampleRSocketStreamHandler =
    new ExampleRSocketStreamHandler(exampleImpl, contentType)

  lazy val applicationRoute: Route = RouteFactory.combine(
    new PostRouteFactory[ExampleRequest]("post-endpoint", postHandler),
    new WebsocketRouteFactory[ExampleRequest]("websocket-endpoint", websocketHandler),
    new SseRouteFactory[ExampleRequest]("sse-endpoint", sseHandler)
  )

  lazy val exampleServer = new ExampleServer(applicationRoute)
  lazy val rSocketServer = new RSocketServer(requestResponseHandler, requestStreamHandler)
}
