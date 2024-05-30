package msocket.jvm.metrics

import io.prometheus.client.{CollectorRegistry, Counter, Gauge}

object Metrics {
  lazy val prometheusRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

  def counter[Req: LabelExtractor](metricName: String, help: String): Counter =
    Counter
      .build()
      .name(metricName)
      .help(help)
      .labelNames(LabelExtractor[Req].allLabelNames*)
      .register(prometheusRegistry)

  def gauge[Req: LabelExtractor](metricName: String, help: String): Gauge =
    Gauge
      .build()
      .name(metricName)
      .help(help)
      .labelNames(LabelExtractor[Req].allLabelNames*)
      .register(prometheusRegistry)
}
