package msocket.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleRequest.{ExampleRequestResponse, ExampleRequestStream}
import csw.example.impl.ExampleImpl
import io.rsocket.RSocket
import msocket.api.ContentType
import msocket.example.server.handlers._
import msocket.impl.metrics.Metrics
import msocket.impl.post.{PostRouteFactory, PostStreamRouteFactory}
import msocket.impl.rsocket.server.{RSocketImpl, RSocketServer}
import msocket.impl.sse.SseRouteFactory
import msocket.impl.ws.WebsocketRouteFactory

import scala.concurrent.ExecutionContext

/** This is where the supported handlers are wired with the server */
class ServerWiring extends ExampleCodecs {
  implicit lazy val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "server")
  implicit lazy val ec: ExecutionContext        = actorSystem.executionContext

  lazy val exampleImpl: ExampleApi = new ExampleImpl

  lazy val postHandler: ExampleHttpPostHandler                            = new ExampleHttpPostHandler(exampleImpl)
  lazy val postStreamHandler: ExampleHttpStreamHandler                    = new ExampleHttpStreamHandler(exampleImpl)
  lazy val sseHandler: ExampleSseHandler                                  = new ExampleSseHandler(exampleImpl)
  def websocketHandler(contentType: ContentType): ExampleWebsocketHandler = new ExampleWebsocketHandler(exampleImpl, contentType)

  def requestResponseHandler(contentType: ContentType): ExampleRSocketResponseHandler =
    new ExampleRSocketResponseHandler(exampleImpl, contentType)
  def requestStreamHandler(contentType: ContentType): ExampleRSocketStreamHandler =
    new ExampleRSocketStreamHandler(exampleImpl, contentType)
  def rSocketFactory(contentType: ContentType): RSocket =
    new RSocketImpl(requestResponseHandler, requestStreamHandler, contentType)

  lazy val applicationRoute: Route =
    new PostRouteFactory[ExampleRequestResponse]("post-endpoint", postHandler).make(List("appName"), metricsEnabled = true) ~
      new PostStreamRouteFactory[ExampleRequestStream]("post-streaming-endpoint", postStreamHandler).make() ~
      new WebsocketRouteFactory[ExampleRequestStream]("websocket-endpoint", websocketHandler).make(metricsEnabled = true) ~
      new SseRouteFactory[ExampleRequestStream]("sse-endpoint", sseHandler).make() ~
      Metrics.metricsRoute

  lazy val exampleServer = new ExampleServer(applicationRoute)(actorSystem)
  lazy val rSocketServer = new RSocketServer(rSocketFactory)

}
