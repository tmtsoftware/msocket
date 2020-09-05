package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.impl.RouteFactory
import msocket.impl.post.ServerHttpCodecs
import msocket.impl.post.headers.AppNameHeader
import msocket.security.api.AccessControllerFactory
import msocket.service.StreamRequestHandler
import msocket.service.metrics.{Labelled, MetricCollector}

class WebsocketRouteFactory[Req: Decoder: ErrorProtocol: Labelled](
    endpoint: String,
    streamRequestHandler: StreamRequestHandler[Req],
    accessControllerFactory: AccessControllerFactory
)(implicit actorSystem: ActorSystem[_])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with WebsocketMetrics {

  import actorSystem.executionContext

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = websocketGauge
    lazy val perMsgCounter = websocketPerMsgCounter

    get {
      path(endpoint) {
        parameters(AppNameHeader.name.optional, Authorization.name.optional) { (appName: Option[String], token: Option[String]) =>
          val accessController = accessControllerFactory.make(token)
          extractClientIP { clientIp =>
            val collectorFactory =
              new MetricCollector[Req](metricsEnabled, _, appName, Some(perMsgCounter), Some(gauge), clientIp.toString())
            handleWebSocketMessages(new WebsocketServerFlow(streamRequestHandler, collectorFactory, accessController).flow)
          }
        }
      }
    }
  }
}
