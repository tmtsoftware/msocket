package msocket.impl

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import msocket.impl.metrics.Metrics
import msocket.service.Labelled

abstract class RouteFactory[T: Labelled] {
  def make(metricsEnabled: Boolean): Route
}

object RouteFactory {
  def combine(metricsEnabled: Boolean)(factory: RouteFactory[_], factories: RouteFactory[_]*): Route = {
    val route = factories.foldLeft(factory.make(metricsEnabled))(_ ~ _.make(metricsEnabled))
    if (metricsEnabled) route ~ Metrics.metricsRoute else route
  }
}
