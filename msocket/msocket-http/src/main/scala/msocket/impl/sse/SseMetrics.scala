package msocket.impl.sse

import io.prometheus.client.{Counter, Gauge}
import msocket.service.metrics.{Labelled, Metrics}

object SseMetrics extends SseMetrics

trait SseMetrics extends Metrics {

  def sseGauge[Req: Labelled]: Gauge =
    gauge(
      metricName = "sse_active_request_total",
      help = "Total active sse connections"
    )

  def ssePerMsgCounter[Req: Labelled]: Counter =
    counter(
      metricName = "sse_total_messages_per_connection",
      help = "Total messages passing through sse connection"
    )
}
