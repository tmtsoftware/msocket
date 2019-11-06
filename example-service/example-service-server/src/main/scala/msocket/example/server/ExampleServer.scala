package msocket.example.server

import akka.http.scaladsl.server.{HttpApp, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

class ExampleServer(_routes: Route) extends HttpApp {
  def routesForTesting: Route = routes

  override protected def routes: Route = cors() {
    _routes
  }
}
