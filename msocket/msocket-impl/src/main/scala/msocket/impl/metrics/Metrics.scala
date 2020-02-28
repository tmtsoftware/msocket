package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.scaladsl.Source
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.prometheus.client.{Counter, Gauge, SimpleCollector}
import msocket.api.{LabelNames, Labelled, RequestMetadata}

object Metrics extends Metrics

trait Metrics {
  private lazy val prometheusRegistry = PrometheusResponseTimeRecorder.DefaultRegistry
  lazy val metricsRoute: Route        = new MetricsEndpoint(prometheusRegistry).routes

  def counter[Req: LabelNames](metricName: String, help: String): Counter =
    Counter
      .build()
      .name(metricName)
      .help(help)
      .labelNames(LabelNames[Req].get: _*)
      .register(prometheusRegistry)

  def gauge[Req: LabelNames](metricName: String, help: String): Gauge =
    Gauge
      .build()
      .name(metricName)
      .help(help)
      .labelNames(LabelNames[Req].get: _*)
      .register(prometheusRegistry)

  def withMetrics[Msg, Req: Labelled](
      source: Source[Msg, NotUsed],
      req: Req,
      metadata: MetricMetadata[Gauge.Child]
  ): Source[Msg, NotUsed] = {
    import metadata._
    if (enabled) {
      val values = labelValues(req, RequestMetadata(hostAddress))
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

  private[metrics] def labelValues[T: Labelled](req: T, requestMetadata: RequestMetadata) =
    Labelled[T].labels(req, requestMetadata).values

}
