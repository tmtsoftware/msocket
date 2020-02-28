package msocket.impl.metrics

import io.prometheus.client.Gauge
import msocket.api.Labelled

object SseMetrics extends SseMetrics

trait SseMetrics extends Metrics {

  private[metrics] val SseGaugeMetricName = "sse_active_request_total"

  def sseGauge[Req: Labelled]: Gauge = gauge(
    metricName = SseGaugeMetricName,
    help = "Total active sse connections"
  )

}
