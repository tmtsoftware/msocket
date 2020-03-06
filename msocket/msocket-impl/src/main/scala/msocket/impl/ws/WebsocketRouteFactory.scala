package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.{ContentType, ErrorProtocol, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.WebsocketMetrics
import msocket.impl.post.ServerHttpCodecs

class WebsocketRouteFactory[Req: Decoder: ErrorProtocol: Labelled](
    endpoint: String,
    websocketHandler: ContentType => WebsocketHandler[Req]
)(implicit actorSystem: ActorSystem[_])
    extends RouteFactory[Req]
    with ServerHttpCodecs
    with WebsocketMetrics {

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val gauge         = websocketGauge
    lazy val perMsgCounter = websocketPerMsgCounter

    get {
      path(endpoint) {
        withPartialMetricCollector[Req](metricsEnabled, Some(perMsgCounter), Some(gauge)).apply { pmc =>
          handleWebSocketMessages(new WebsocketServerFlow(websocketHandler, pmc).flow)
        }
      }
    }
  }
}
