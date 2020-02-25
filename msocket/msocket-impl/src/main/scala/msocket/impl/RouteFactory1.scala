package msocket.impl

import akka.http.scaladsl.server.Route
import msocket.api.Labellable

trait RouteFactory1[T] {
  def make(labelNames: List[String])(implicit labels: T => Labellable[T]): Route
}