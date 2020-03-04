package msocket.impl.metrics

import akka.http.scaladsl.model.RemoteAddress
import akka.http.scaladsl.server.RequestContext
import io.prometheus.client.SimpleCollector

import scala.concurrent.ExecutionContext

class MetricMetadata[T](
    val enabled: Boolean,
    _collector: => SimpleCollector[T],
    private val clientAddress: RemoteAddress,
    requestContext: RequestContext
) {
  lazy val collector: SimpleCollector[T] = _collector
  implicit lazy val ec: ExecutionContext = requestContext.executionContext

  val clientIp: String = clientAddress.toString()
}
