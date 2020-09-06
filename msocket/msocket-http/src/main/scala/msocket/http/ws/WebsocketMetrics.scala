package msocket.http.ws

import io.prometheus.client.{Counter, Gauge}
import msocket.jvm.metrics.{LabelExtractor, Metrics}

object WebsocketMetrics {
  def gauge[Req: LabelExtractor](): Gauge =
    Metrics.gauge(
      metricName = "websocket_active_request_total",
      help = "Total active websocket connections"
    )

  def counter[Req: LabelExtractor](): Counter =
    Metrics.counter(
      metricName = "websocket_total_messages_per_connection",
      help = "Total messages passing through websocket connection"
    )
}
