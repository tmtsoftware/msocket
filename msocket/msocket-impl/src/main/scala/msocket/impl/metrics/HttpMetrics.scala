package msocket.impl.metrics

import io.prometheus.client.Counter
import msocket.api.models.MetricLabels

object HttpMetrics extends HttpMetrics

trait HttpMetrics extends Metrics {

  private[metrics] val httpCounterMetricName = "gateway_http_requests_total"
  def httpCounter(labelsNames: List[String]): Counter =
    counter(
      metricName = httpCounterMetricName,
      help = "Total http requests",
      labelNames = MetricLabels.DefaultLabels ++ labelsNames: _*
    )

}
