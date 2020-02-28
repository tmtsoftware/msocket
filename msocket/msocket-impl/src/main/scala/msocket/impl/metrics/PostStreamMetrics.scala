package msocket.impl.metrics

import io.prometheus.client.Gauge
import msocket.api.Labelled

trait PostStreamMetrics extends Metrics {

  private[metrics] val PostStreamGaugeMetricName = "post_stream_active_request_total"

  def postStreamGauge[Req: Labelled]: Gauge = gauge(
    metricName = PostStreamGaugeMetricName,
    help = "Total active post stream connections"
  )

}
