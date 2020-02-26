package msocket.impl.metrics

import io.prometheus.client.Gauge

object SseMetrics extends SseMetrics

trait SseMetrics extends Metrics {

  private[metrics] val sseGaugeMetricName = "sse_active_request_total"
  def sseGauge(labelsNames: List[String]): Gauge =
    gauge(
      metricName = sseGaugeMetricName,
      help = "Total active sse connections",
      labelNames = labelsNames
    )

}
