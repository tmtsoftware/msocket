package msocket.impl.metrics

import akka.http.scaladsl.server.Directives.{as, entity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.HostDirectives.extractHost
import io.bullet.borer.Decoder
import io.prometheus.client.Counter
import msocket.api.{ErrorProtocol, LabelNames, Labelled}
import msocket.impl.post.ServerHttpCodecs._

object HttpMetrics extends HttpMetrics

trait HttpMetrics extends Metrics {

  private[metrics] val HttpCounterMetricName = "http_requests_total"

  def httpCounter[Req: LabelNames]: Counter = counter(
    metricName = HttpCounterMetricName,
    help = "Total http requests"
  )

  def withHttpMetrics[Req: Decoder: ErrorProtocol: Labelled](metadata: MetricMetadata[Counter.Child], handle: Req => Route): Route = {
    import metadata._
    if (enabled)
      extractHost { address =>
        entity(as[Req]) { req =>
          val labels = labelValues(req, address)
          collector.labels(labels: _*).inc()
          handle(req)
        }
      } else entity(as[Req])(handle)
  }
}
