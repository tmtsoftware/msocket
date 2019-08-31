package msocket.example.server

import akka.http.scaladsl.server.{HttpApp, Route}

class ExampleServer(_routes: Route) extends HttpApp {
  def routesForTesting: Route = routes

  override protected def routes: Route = _routes
}
