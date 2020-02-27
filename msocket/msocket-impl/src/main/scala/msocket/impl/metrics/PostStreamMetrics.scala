package msocket.impl.metrics

import io.prometheus.client.Gauge
import msocket.api.LabelNames

trait PostStreamMetrics extends Metrics {

  private[metrics] val PostStreamGaugeMetricName = "post_stream_active_request_total"

  def postStreamGauge[Req: LabelNames]: Gauge = gauge(
    metricName = PostStreamGaugeMetricName,
    help = "Total active post stream connections"
  )

}
