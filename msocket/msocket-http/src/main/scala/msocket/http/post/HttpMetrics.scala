package msocket.http.post

import io.prometheus.client.Counter
import msocket.jvm.metrics.{LabelExtractor, Metrics}

object HttpMetrics {
  def counter[Req: LabelExtractor]: Counter =
    Metrics.counter(
      metricName = "http_requests_total",
      help = "Total http requests"
    )

  def counter2[Req: LabelExtractor]: Counter =
    Metrics.counter(
      metricName = "http_requests_total2",
      help = "Total http requests2"
    )
}