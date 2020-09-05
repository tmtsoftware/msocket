package msocket.impl

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import msocket.service.metrics.{Labelled, Metrics}

abstract class RouteFactory[T: Labelled] {
  def make(metricsEnabled: Boolean): Route
}

object RouteFactory {
  lazy val metricsRoute: Route = new MetricsEndpoint(Metrics.prometheusRegistry).routes

  def combine(metricsEnabled: Boolean)(factory: RouteFactory[_], factories: RouteFactory[_]*): Route = {
    val route = factories.foldLeft(factory.make(metricsEnabled))(_ ~ _.make(metricsEnabled))
    if (metricsEnabled) route ~ metricsRoute else route
  }
}
