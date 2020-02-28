package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.scaladsl.Source
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.prometheus.client.{Counter, Gauge, SimpleCollector}
import msocket.api.{Labelled, RequestMetadata}

object Metrics extends Metrics

trait Metrics {
  private lazy val prometheusRegistry = PrometheusResponseTimeRecorder.DefaultRegistry
  lazy val metricsRoute: Route        = new MetricsEndpoint(prometheusRegistry).routes

  def counter[Req: Labelled](metricName: String, help: String): Counter =
    Counter
      .build()
      .name(metricName)
      .help(help)
      .labelNames(Labelled[Req].labelNames: _*)
      .register(prometheusRegistry)

  def gauge[Req: Labelled](metricName: String, help: String): Gauge =
    Gauge
      .build()
      .name(metricName)
      .help(help)
      .labelNames(Labelled[Req].labelNames: _*)
      .register(prometheusRegistry)

  def withMetrics[Msg, Req: Labelled](
      source: Source[Msg, NotUsed],
      req: Req,
      metadata: MetricMetadata[Gauge.Child]
  ): Source[Msg, NotUsed] = {
    import metadata._
    if (enabled) {
      val values = Labelled[Req].labels(req, RequestMetadata(hostAddress)).values
      val child  = collector.labels(values: _*)
      child.inc()
      source.watchTermination() {
        case (mat, completion) =>
          completion.onComplete(_ => child.dec())
          mat
      }
    } else source
  }

  def withMetricMetadata[T](enabled: Boolean, collector: => SimpleCollector[T]): Directive1[MetricMetadata[T]] =
    extract(new MetricMetadata(enabled, collector, _))

}
