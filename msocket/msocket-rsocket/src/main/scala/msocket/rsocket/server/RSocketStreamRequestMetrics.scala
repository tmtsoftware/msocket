package msocket.rsocket.server

import io.prometheus.client.{Counter, Gauge}
import msocket.jvm.metrics.{Labelled, Metrics}

trait RSocketStreamRequestMetrics extends Metrics {

  def rSocketStreamGauge[Req: Labelled]: Gauge =
    gauge(
      metricName = "rsocket_stream_active_request_total",
      help = "Total active rsocket streaming channels"
    )

  def rSocketStreamPerMsgCounter[Req: Labelled]: Counter =
    counter(
      metricName = "rsocket_stream_total_messages_per_connection",
      help = "Total messages passing through rsocket channel"
    )
}
