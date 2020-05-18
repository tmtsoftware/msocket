package msocket.impl.metrics

import io.prometheus.client.{Counter, Gauge}
import msocket.api.Labelled

object WebsocketMetrics extends WebsocketMetrics

trait WebsocketMetrics extends Metrics {

  def websocketGauge[Req: Labelled]: Gauge =
    gauge(
      metricName = "websocket_active_request_total",
      help = "Total active websocket connections"
    )

  def websocketPerMsgCounter[Req: Labelled]: Counter =
    counter(
      metricName = "websocket_total_messages_per_connection",
      help = "Total messages passing through websocket connection"
    )
}
