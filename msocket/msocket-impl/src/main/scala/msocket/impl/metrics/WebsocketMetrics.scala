package msocket.impl.metrics

import io.prometheus.client.Gauge
import msocket.api.models.MetricLabels

object WebsocketMetrics extends Metrics

trait WebsocketMetrics extends Metrics {

  private[metrics] val websocketGaugeMetricName = "gateway_websocket_active_request_total"
  def websocketGauge(labelsNames: List[String]): Gauge =
    gauge(
      metricName = websocketGaugeMetricName,
      help = "Total active websocket connections",
      labelNames = MetricLabels.DefaultLabels ++ labelsNames: _*
    )
}
