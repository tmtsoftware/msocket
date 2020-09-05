package msocket.http.post.streaming

import io.prometheus.client.{Counter, Gauge}
import msocket.jvm.metrics.{Labelled, Metrics}

trait PostStreamMetrics extends Metrics {

  def postStreamGauge[Req: Labelled]: Gauge =
    gauge(
      metricName = "post_stream_active_request_total",
      help = "Total active post stream connections"
    )

  def postStreamPerMsgCounter[Req: Labelled]: Counter =
    counter(
      metricName = "post_stream_total_messages_per_connection",
      help = "Total messages passing through post stream connection"
    )
}
