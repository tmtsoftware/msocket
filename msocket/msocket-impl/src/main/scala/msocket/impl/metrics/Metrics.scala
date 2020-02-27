package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.scaladsl.Source
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.prometheus.client.{Counter, Gauge, SimpleCollector}
import msocket.api.Labelled

object Metrics extends Metrics

trait Metrics {
  private lazy val prometheusRegistry = PrometheusResponseTimeRecorder.DefaultRegistry
  lazy val metricsRoute: Route        = new MetricsEndpoint(prometheusRegistry).routes

  def counter(metricName: String, help: String, labelNames: List[String]): Counter =
    Counter
      .build()
      .name(metricName)
      .help(help)
      .labelNames(labelNames: _*)
      .register(prometheusRegistry)

  def gauge(metricName: String, help: String, labelNames: List[String]): Gauge =
    Gauge
      .build()
      .name(metricName)
      .help(help)
      .labelNames(labelNames: _*)
      .register(prometheusRegistry)

  def withMetrics[Msg, Req](source: Source[Msg, NotUsed], req: Req, metadata: MetricMetadata[Gauge.Child])(
      implicit labelGen: Req => Labelled[Req]
  ): Source[Msg, NotUsed] = {
    import metadata._
    if (enabled) {
      val values = labelValues(req, labelGen(req), hostAddress)
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
    extract(MetricMetadata(enabled, collector, _))

  private[metrics] def labelValues[T](req: T, labelled: Labelled[T], address: String) =
    labelled.labels().withMandatoryLabels(req, address).values

}
