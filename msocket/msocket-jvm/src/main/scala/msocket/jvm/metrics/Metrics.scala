package msocket.jvm.metrics

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.prometheus.client.{CollectorRegistry, Counter, Gauge}

object Metrics extends Metrics

trait Metrics {
  lazy val prometheusRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

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

  def record[Req](collector: MetricCollector[Req]): Unit = {
    import collector._
    if (enabled) incCounter()
  }

}
