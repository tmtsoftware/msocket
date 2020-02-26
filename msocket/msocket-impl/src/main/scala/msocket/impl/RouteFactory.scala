package msocket.impl

import akka.http.scaladsl.server.Route
import msocket.api.Labelled

trait RouteFactory[T] {
  def make(metricsEnabled: Boolean = false)(implicit labelGen: T => Labelled[T]): Route
}
