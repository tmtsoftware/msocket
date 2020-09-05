package msocket.rsocket.server

import io.prometheus.client.Counter
import msocket.jvm.metrics.{Labelled, Metrics}

trait RSocketMonoRequestMetrics extends Metrics {

  def rSocketMonoCounter[Req: Labelled]: Counter =
    counter(
      metricName = "rsocket_mono_requests_total",
      help = "Total rsocket mono requests"
    )

}
