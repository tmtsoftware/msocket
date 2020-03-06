package msocket.impl.metrics

import akka.http.scaladsl.model.RemoteAddress
import akka.http.scaladsl.server.RequestContext
import io.prometheus.client.{Counter, Gauge}

import scala.concurrent.ExecutionContext

class MetricMetadata(
    val enabled: Boolean,
    _counter: => Option[Counter],
    _gauge: => Option[Gauge],
    private val clientAddress: RemoteAddress,
    requestContext: RequestContext
) {
  lazy val metricsCounter: Option[Counter] = _counter
  lazy val metricsGauge: Option[Gauge]     = _gauge
  implicit lazy val ec: ExecutionContext   = requestContext.executionContext

  val clientIp: String = clientAddress.toString()

  def incGauge(labels: List[String]): Unit   = metricsGauge.foreach(_.labels(labels: _*).inc())
  def decGauge(labels: List[String]): Unit   = metricsGauge.foreach(_.labels(labels: _*).dec())
  def incCounter(labels: List[String]): Unit = metricsCounter.foreach(_.labels(labels: _*).inc())
}
