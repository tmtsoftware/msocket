package msocket.impl.metrics

import io.prometheus.client.Counter

object HttpMetrics extends HttpMetrics

trait HttpMetrics extends Metrics {

  private[metrics] val httpCounterMetricName = "http_requests_total"
  def httpCounter(labelsNames: List[String]): Counter =
    counter(
      metricName = httpCounterMetricName,
      help = "Total http requests",
      labelNames = labelsNames
    )

}
