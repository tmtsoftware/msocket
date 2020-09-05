package msocket.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.impl.handlers.ExampleStreamRequestHandler
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
import csw.example.impl.ExampleImpl
import io.rsocket.RSocket
import msocket.api.ContentType
import msocket.example.server.handlers._
import msocket.impl.RouteFactory
import msocket.impl.post.PostRouteFactory
import msocket.impl.post.streaming.PostStreamRouteFactory
import msocket.impl.rsocket.server.{RSocketImpl, RSocketServer}
import msocket.impl.sse.SseRouteFactory
import msocket.impl.ws.WebsocketRouteFactory
import msocket.security.AccessControllerFactory
import msocket.security.api.TokenValidator
import msocket.security.models.AccessToken
import msocket.service.metrics.Labelled

import scala.concurrent.{ExecutionContext, Future}

/** This is where the supported handlers are wired with the server */
class ServerWiring extends ExampleCodecs {
  implicit lazy val actorSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "server")
  implicit lazy val ec: ExecutionContext        = actorSystem.executionContext

  lazy val exampleImpl: ExampleApi = new ExampleImpl

  lazy val postHandler: ExampleHttpPostHandler               = new ExampleHttpPostHandler(exampleImpl)
  lazy val exampleStreamHandler: ExampleStreamRequestHandler = new ExampleStreamRequestHandler(exampleImpl)

  def requestResponseHandler(contentType: ContentType): ExampleRSocketResponseHandler =
    new ExampleRSocketResponseHandler(exampleImpl, contentType)
  def rSocketFactory(contentType: ContentType): RSocket                               =
    new RSocketImpl(requestResponseHandler, exampleStreamHandler, contentType, accessControllerFactory)

  lazy val tokenValidator: TokenValidator = _ => Future.successful(AccessToken())
  lazy val isSecurityEnabled: Boolean     = false
  lazy val accessControllerFactory        = new AccessControllerFactory(tokenValidator, isSecurityEnabled)

  private val testLabel                           = "test_label"
  implicit val labelled: Labelled[ExampleRequest] = Labelled.make(List(testLabel)) {
    case _ => Map(testLabel -> "test_value")
  }

  lazy val applicationRoute: Route = RouteFactory.combine(metricsEnabled = true)(
    new PostRouteFactory[ExampleRequest]("post-endpoint", postHandler),
    new PostStreamRouteFactory[ExampleStreamRequest]("post-streaming-endpoint", exampleStreamHandler, accessControllerFactory),
    new WebsocketRouteFactory[ExampleStreamRequest]("websocket-endpoint", exampleStreamHandler, accessControllerFactory),
    new SseRouteFactory[ExampleStreamRequest]("sse-endpoint", exampleStreamHandler, accessControllerFactory)
  )

  lazy val exampleServer = new ExampleServer(applicationRoute)(actorSystem)
  lazy val rSocketServer = new RSocketServer(rSocketFactory)

}
