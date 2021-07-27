package msocket.http.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.ServerHttpCodecs
import msocket.jvm.metrics.{LabelExtractor, MetricCollector}
import msocket.jvm.stream.StreamRequestHandler
import msocket.security.AccessControllerFactory

class WebsocketRouteFactory[Req: Decoder: ErrorProtocol: LabelExtractor](
    endpoint: String,
    streamRequestHandler: StreamRequestHandler[Req]
)(implicit actorSystem: ActorSystem[_])
    extends RouteFactory[Req]
    with ServerHttpCodecs {

  import actorSystem.executionContext

  private val accessControllerFactory = AccessControllerFactory.noop
  private val appNameParam            = "appName"
  private val usernameParam           = "username"

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = WebsocketMetrics.gauge()
    lazy val perMsgCounter = WebsocketMetrics.counter()

    get {
      path(endpoint) {
        parameters(appNameParam.optional, usernameParam.optional, Authorization.name.optional) { (appName, username, token) =>
          val accessController = accessControllerFactory.make(token)
          val collectorFactory =
            new MetricCollector[Req](metricsEnabled, _, appName, username, Some(perMsgCounter), Some(gauge))
          handleWebSocketMessages(new WebsocketServerFlow(streamRequestHandler, collectorFactory, accessController).flow)
        }
      }
    }
  }
}
