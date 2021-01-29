package msocket.http.sse

import io.prometheus.client.{Counter, Gauge}
import msocket.jvm.metrics.{LabelExtractor, Metrics}

object SseMetrics {
  def gauge[Req: LabelExtractor](): Gauge =
    Metrics.gauge(
      metricName = "sse_active_request_total",
      help = "Total active sse connections"
    )

  def counter[Req: LabelExtractor](): Counter =
    Metrics.counter(
      metricName = "sse_messages_per_connection_total",
      help = "Total messages passing through sse connection"
    )
}
