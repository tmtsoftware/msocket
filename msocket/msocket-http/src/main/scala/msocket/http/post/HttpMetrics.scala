package msocket.http.post

import io.prometheus.client.Counter
import msocket.jvm.metrics.{Labelled, Metrics}

object HttpMetrics extends HttpMetrics

trait HttpMetrics extends Metrics {

  def httpCounter[Req: Labelled]: Counter =
    counter(
      metricName = "http_requests_total",
      help = "Total http requests"
    )
}
