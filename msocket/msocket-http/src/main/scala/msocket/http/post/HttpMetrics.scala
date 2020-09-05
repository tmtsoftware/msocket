package msocket.http.post

import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import io.prometheus.client.Counter
import msocket.api.ErrorProtocol
import msocket.jvm.metrics.{Labelled, MetricCollector, Metrics}

object HttpMetrics extends HttpMetrics

trait HttpMetrics extends Metrics {

  def httpCounter[Req: Labelled]: Counter =
    counter(
      metricName = "http_requests_total",
      help = "Total http requests"
    )

  def withHttpMetrics[Req: Decoder: ErrorProtocol: Labelled](collector: MetricCollector[Req], handle: Req => Route): Route = {
    import collector._
    if (enabled) incCounter()
    handle(request)
  }
}
