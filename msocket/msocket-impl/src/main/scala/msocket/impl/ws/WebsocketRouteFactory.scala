package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.{ContentType, ErrorProtocol, LabelNames, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.WebsocketMetrics
import msocket.impl.post.ServerHttpCodecs

class WebsocketRouteFactory[Req: Decoder: ErrorProtocol: LabelNames](
    endpoint: String,
    websocketHandler: ContentType => WebsocketHandler[Req]
)(
    implicit actorSystem: ActorSystem[_]
) extends RouteFactory[Req]
    with ServerHttpCodecs
    with WebsocketMetrics {

  def make(metricsEnabled: Boolean = false)(implicit labelGen: Req => Labelled[Req]): Route = {
    lazy val gauge = websocketGauge(LabelNames[Req].get)
    get {
      path(endpoint) {
        extractHost { hostAddress =>
          handleWebSocketMessages {
            new WebsocketServerFlow(websocketHandler, metricsEnabled, gauge, hostAddress).flow
          }
        }
      }
    }
  }
}
