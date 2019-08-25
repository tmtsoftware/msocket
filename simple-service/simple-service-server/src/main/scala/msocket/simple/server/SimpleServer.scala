package msocket.simple.server

import akka.http.scaladsl.server.{HttpApp, Route}

class SimpleServer(_routes: Route) extends HttpApp {
  def routesForTesting: Route = routes

  override protected def routes: Route = _routes
}
