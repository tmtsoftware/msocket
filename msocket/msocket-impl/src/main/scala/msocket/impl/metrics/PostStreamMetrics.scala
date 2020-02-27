package msocket.impl.metrics

import io.prometheus.client.Gauge

trait PostStreamMetrics extends Metrics {

  private[metrics] val postStreamGaugeMetricName = "post_stream_active_request_total"
  def postStreamGauge(labelsNames: List[String]): Gauge =
    gauge(
      metricName = postStreamGaugeMetricName,
      help = "Total active post stream connections",
      labelNames = labelsNames
    )

}
