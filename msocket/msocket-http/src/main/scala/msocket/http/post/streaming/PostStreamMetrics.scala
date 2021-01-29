package msocket.http.post.streaming

import io.prometheus.client.{Counter, Gauge}
import msocket.jvm.metrics.{LabelExtractor, Metrics}

object PostStreamMetrics {
  def gauge[Req: LabelExtractor](): Gauge =
    Metrics.gauge(
      metricName = "post_stream_active_request_total",
      help = "Total active post stream connections"
    )

  def counter[Req: LabelExtractor](): Counter =
    Metrics.counter(
      metricName = "post_stream_messages_per_connection_total",
      help = "Total messages passing through post stream connection"
    )
}
