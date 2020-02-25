package msocket.impl

import akka.http.scaladsl.server.Route
import msocket.api.Labelled

trait RouteFactory1[T] {
  def make(labelNames: List[String], metricsEnabled: Boolean)(implicit labels: T => Labelled[T]): Route
}