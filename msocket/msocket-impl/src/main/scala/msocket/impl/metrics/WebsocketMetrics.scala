package msocket.impl.metrics

import io.prometheus.client.Gauge
import msocket.api.LabelNames

object WebsocketMetrics extends WebsocketMetrics

trait WebsocketMetrics extends Metrics {

  private[metrics] val WebsocketGaugeMetricName = "websocket_active_request_total"

  def websocketGauge[Req: LabelNames]: Gauge = gauge(
    metricName = WebsocketGaugeMetricName,
    help = "Total active websocket connections"
  )

}
