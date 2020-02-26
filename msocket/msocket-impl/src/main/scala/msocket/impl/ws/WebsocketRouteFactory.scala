package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import msocket.api.{ContentType, ErrorProtocol, Labelled}
import msocket.impl.RouteFactory
import msocket.impl.metrics.Metrics
import msocket.impl.post.ServerHttpCodecs

class WebsocketRouteFactory[Req: Decoder: ErrorProtocol](endpoint: String, websocketHandler: ContentType => WebsocketHandler[Req])(
    implicit actorSystem: ActorSystem[_]
) extends RouteFactory[Req]
    with ServerHttpCodecs {

  override def make(labelNames: List[String] = List.empty, metricsEnabled: Boolean = false)(
      implicit labelGen: Req => Labelled[Req]
  ): Route = {
    lazy val gauge = Metrics.websocketGauge(labelNames)

    get {
      path(endpoint) {
        extractRequest { httpRequest =>
          val hostAddress = httpRequest.uri.authority.host.address

          handleWebSocketMessages {
            new WebsocketServerFlow(websocketHandler, metricsEnabled, gauge, hostAddress).flow
          }
        }
      }
    }
  }
}
