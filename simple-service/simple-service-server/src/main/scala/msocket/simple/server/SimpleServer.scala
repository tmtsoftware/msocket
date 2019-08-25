package msocket.simple.server

import akka.http.scaladsl.server.{HttpApp, Route}
import csw.simple.api.RequestProtocol
import mscoket.impl.{Encoding, WsServerFlow}

class SimpleServer(wsServerFlow: WsServerFlow[RequestProtocol]) extends HttpApp {
  def routesForTesting: Route = routes

  override protected def routes: Route = path("websocket" / Segment) { encoding =>
    handleWebSocketMessages(wsServerFlow.flow(Encoding.fromString(encoding)))
  }
}
