package msocket.example.server

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
import csw.example.impl.ExampleImpl
import csw.example.impl.handlers.{ExampleMonoRequestHandler, ExampleStreamRequestHandler}
import io.rsocket.RSocket
import msocket.api.ContentType
import msocket.example.server.handlers._
import msocket.http.RouteFactory
import msocket.http.post.streaming.PostStreamRouteFactory
import msocket.http.post.{PostRouteFactory, PostRouteFactory2}
import msocket.http.sse.SseRouteFactory
import msocket.http.ws.WebsocketRouteFactory
import msocket.jvm.metrics.LabelExtractor
import msocket.rsocket.server._
import msocket.security.AccessControllerFactory

import scala.concurrent.ExecutionContext

/** This is where the supported handlers are wired with the server */
class ServerWiring extends ExampleCodecs {
  implicit lazy val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "server")
  implicit lazy val ec: ExecutionContext        = actorSystem.executionContext

  private val testLabel                                              = "test_label"
  implicit val requestLabelExtractor: LabelExtractor[ExampleRequest] = LabelExtractor.make(List(testLabel)) { case _ =>
    Map(testLabel -> "test_value")
  }

  lazy val exampleImpl: ExampleApi = new ExampleImpl

  lazy val postHandler: ExampleHttpPostHandler               = new ExampleHttpPostHandler(exampleImpl)
  lazy val exampleStreamHandler: ExampleStreamRequestHandler = new ExampleStreamRequestHandler(exampleImpl)
  lazy val exampleMonoHandler: ExampleMonoRequestHandler     = new ExampleMonoRequestHandler(exampleImpl)

  import LabelExtractor.Implicits.default
  def rSocketFactory(contentType: ContentType): RSocket =
    new RSocketImpl(exampleMonoHandler, exampleStreamHandler, contentType, AccessControllerFactory.noop)

  lazy val applicationRoute: Route = RouteFactory.combine(metricsEnabled = true)(
    new PostRouteFactory[ExampleRequest]("post-endpoint", postHandler),
    new PostRouteFactory2[ExampleRequest]("post-endpoint2", exampleMonoHandler, AccessControllerFactory.noop),
    new PostStreamRouteFactory[ExampleStreamRequest]("post-streaming-endpoint", exampleStreamHandler, AccessControllerFactory.noop),
    new WebsocketRouteFactory[ExampleStreamRequest]("websocket-endpoint", exampleStreamHandler),
    new SseRouteFactory[ExampleStreamRequest]("sse-endpoint", exampleStreamHandler, AccessControllerFactory.noop)
  )

  lazy val exampleServer = new ExampleServer(applicationRoute)(actorSystem)
  lazy val rSocketServer = new RSocketServer(rSocketFactory)

}
