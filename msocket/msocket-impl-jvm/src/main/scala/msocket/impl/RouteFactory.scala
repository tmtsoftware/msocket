package msocket.impl

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait RouteFactory {
  def make(): Route
}

object RouteFactory {
  def combine(factory: RouteFactory, factories: RouteFactory*): Route = factories.foldLeft(factory.make())(_ ~ _.make())
}
