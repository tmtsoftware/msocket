package msocket.rsocket.server

import io.prometheus.client.Counter
import msocket.jvm.metrics.{LabelExtractor, Metrics}

object RSocketMonoRequestMetrics {

  def counter[Req: LabelExtractor]: Counter =
    Metrics.counter(
      metricName = "rsocket_mono_requests_total",
      help = "Total rsocket mono requests"
    )

}
