package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.{ContentType, ErrorProtocol, LabelNames, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.{MetricMetadata, WebsocketMetrics}
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
        extractHost { address =>
          handleWebSocketMessages {
            val metadata = MetricMetadata(metricsEnabled, address, gauge)
            new WebsocketServerFlow(websocketHandler, metadata).flow
          }
        }
      }
    }
  }
}
