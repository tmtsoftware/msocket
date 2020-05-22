package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.server.Directives.extractClientIP
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.scaladsl.Source
import io.prometheus.client.{CollectorRegistry, Counter, Gauge}
import msocket.api.Labelled

object Metrics extends Metrics

trait Metrics {
  private lazy val prometheusRegistry = CollectorRegistry.defaultRegistry
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
      collector: MetricCollector[Req]
  ): Source[Msg, NotUsed] = {
    import collector._
    if (enabled) {
      incGauge()
      source
        .wireTap { _ => incCounter() }
        .watchTermination() {
          case (mat, completion) =>
            completion.onComplete(_ => decGauge())
            mat
        }
    } else source
  }

  def withMetricCollector[Req: Labelled](
      enabled: Boolean,
      req: Req,
      counter: => Option[Counter] = None,
      gauge: => Option[Gauge] = None
  ): Directive1[MetricCollector[Req]] =
    withPartialMetricCollector[Req](enabled, counter, gauge).map(_.apply(req))

  def withPartialMetricCollector[Req: Labelled](
      enabled: Boolean,
      counter: => Option[Counter] = None,
      gauge: => Option[Gauge] = None
  ): Directive1[Req => MetricCollector[Req]] =
    extractClientIP.flatMap { clientIp =>
      extract(ctx => new MetricCollector(enabled, _, counter, gauge, clientIp, ctx))
    }

}
