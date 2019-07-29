package msocket.simple.server

import akka.http.scaladsl.server.{HttpApp, Route}
import csw.simple.api.Protocol
import msocket.core.server.WsServerFlow

class SimpleServer(wsServerFlow: WsServerFlow[Protocol]) extends HttpApp {
  def routesForTesting: Route = routes

  override protected def routes: Route = path("websocket") {
    handleWebSocketMessages(wsServerFlow.flow)
  }
}
