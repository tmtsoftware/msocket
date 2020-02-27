package msocket.impl.metrics

import akka.http.scaladsl.server.RequestContext
import io.prometheus.client.SimpleCollector

import scala.concurrent.ExecutionContext

class MetricMetadata[T](val enabled: Boolean, _collector: => SimpleCollector[T], requestContext: RequestContext) {
  lazy val collector: SimpleCollector[T] = _collector
  lazy val hostAddress: String           = requestContext.request.uri.authority.host.address()
  implicit lazy val ec: ExecutionContext = requestContext.executionContext
}

object MetricMetadata {
  def apply[T](enabled: Boolean, _collector: => SimpleCollector[T], requestContext: RequestContext): MetricMetadata[T] =
    new MetricMetadata(enabled, _collector, requestContext)
}
