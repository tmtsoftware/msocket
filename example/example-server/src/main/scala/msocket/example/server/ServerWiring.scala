package msocket.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
import csw.example.impl.ExampleImpl
import csw.example.impl.handlers.{ExampleMonoRequestHandler, ExampleStreamRequestHandler}
import io.rsocket.RSocket
import msocket.api.ContentType
import msocket.example.server.handlers._
import msocket.http.RouteFactory
import msocket.http.post.PostRouteFactory
import msocket.http.post.streaming.PostStreamRouteFactory
import msocket.http.sse.SseRouteFactory
import msocket.http.ws.WebsocketRouteFactory
import msocket.jvm.metrics.Labelled
import msocket.rsocket.server.{RSocketImpl, RSocketServer}
import msocket.security.AccessControllerFactory

import scala.concurrent.ExecutionContext

/** This is where the supported handlers are wired with the server */
class ServerWiring extends ExampleCodecs {
  implicit lazy val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "server")
  implicit lazy val ec: ExecutionContext        = actorSystem.executionContext

  lazy val exampleImpl: ExampleApi = new ExampleImpl

  lazy val postHandler: ExampleHttpPostHandler                = new ExampleHttpPostHandler(exampleImpl)
  lazy val exampleStreamHandler: ExampleStreamRequestHandler  = new ExampleStreamRequestHandler(exampleImpl)
  lazy val requestResponseHandler: ExampleMonoRequestHandler = new ExampleMonoRequestHandler(exampleImpl)

  def rSocketFactory(contentType: ContentType): RSocket =
    new RSocketImpl(requestResponseHandler, exampleStreamHandler, contentType, AccessControllerFactory.noOp)

  private val testLabel                           = "test_label"
  implicit val labelled: Labelled[ExampleRequest] = Labelled.make(List(testLabel)) {
    case _ => Map(testLabel -> "test_value")
  }

  lazy val applicationRoute: Route = RouteFactory.combine(metricsEnabled = true)(
    new PostRouteFactory[ExampleRequest]("post-endpoint", postHandler),
    new PostStreamRouteFactory[ExampleStreamRequest]("post-streaming-endpoint", exampleStreamHandler, AccessControllerFactory.noOp),
    new WebsocketRouteFactory[ExampleStreamRequest]("websocket-endpoint", exampleStreamHandler),
    new SseRouteFactory[ExampleStreamRequest]("sse-endpoint", exampleStreamHandler, AccessControllerFactory.noOp)
  )

  lazy val exampleServer = new ExampleServer(applicationRoute)(actorSystem)
  lazy val rSocketServer = new RSocketServer(rSocketFactory)

}
