package msocket.impl

import akka.http.scaladsl.server.Route
import msocket.api.Labelled

trait RouteFactory[T] {
  def make(labelNames: List[String], metricsEnabled: Boolean)(implicit labelGen: T => Labelled[T]): Route
}