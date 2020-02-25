package msocket.impl.metrics

import akka.http.scaladsl.server.Route
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.prometheus.client.{CollectorRegistry, Counter, Gauge}
import msocket.api.models.MetricLabels

object Metrics {
  private[msocket] val prometheusRegistry: CollectorRegistry = PrometheusResponseTimeRecorder.DefaultRegistry

  val metricsRoute: Route = new MetricsEndpoint(prometheusRegistry).routes

  private[metrics] val httpCounterMetricName = "gateway_http_requests_total"
  def httpCounter(labelsNames: List[String]): Counter =
    counter(
      metricName = httpCounterMetricName,
      help = "Total http requests",
      labelNames = MetricLabels.DefaultLabels ++ labelsNames: _*
    )

  private[metrics] val websocketGaugeMetricName = "gateway_websocket_active_request_total"
  def websocketGauge(labelsNames: List[String]): Gauge =
    gauge(
      metricName = websocketGaugeMetricName,
      help = "Total active websocket connections",
      labelNames = MetricLabels.DefaultLabels ++ labelsNames: _*
    )

  def counter(metricName: String, help: String, labelNames: String*): Counter =
    Counter
      .build()
      .name(metricName)
      .help(help)
      .labelNames(labelNames: _*)
      .register(prometheusRegistry)

  def gauge(metricName: String, help: String, labelNames: String*): Gauge =
    Gauge
      .build()
      .name(metricName)
      .help(help)
      .labelNames(labelNames: _*)
      .register(prometheusRegistry)

}
