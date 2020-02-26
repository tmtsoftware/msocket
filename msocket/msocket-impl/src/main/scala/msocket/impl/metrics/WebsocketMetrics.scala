package msocket.impl.metrics

import io.prometheus.client.Gauge

object WebsocketMetrics extends WebsocketMetrics

trait WebsocketMetrics extends Metrics {

  private[metrics] val websocketGaugeMetricName = "websocket_active_request_total"
  def websocketGauge(labelsNames: List[String]): Gauge =
    gauge(
      metricName = websocketGaugeMetricName,
      help = "Total active websocket connections",
      labelNames = labelsNames
    )

}
