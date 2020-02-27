package msocket.impl

import akka.http.scaladsl.server.Route
import msocket.api.Labelled

abstract class RouteFactory[T: Labelled] {
  def make(metricsEnabled: Boolean = false): Route
}
