package msocket.rsocket.server

import io.prometheus.client.{Counter, Gauge}
import msocket.jvm.metrics.{LabelExtractor, Metrics}

object RSocketStreamRequestMetrics {
  def gauge[Req: LabelExtractor](): Gauge =
    Metrics.gauge(
      metricName = "rsocket_stream_active_request_total",
      help = "Total active rsocket streaming channels"
    )

  def counter[Req: LabelExtractor](): Counter =
    Metrics.counter(
      metricName = "rsocket_stream_messages_per_connection_total",
      help = "Total messages passing through rsocket channel"
    )
}
