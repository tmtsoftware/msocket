package msocket.impl.metrics

import io.prometheus.client.SimpleCollector

class MetricMetadata[T](val enabled: Boolean, val hostAddress: String, _collector: => SimpleCollector[T]) {
  lazy val collector: SimpleCollector[T] = _collector
}

object MetricMetadata {
  def apply[T](enabled: Boolean, hostAddress: String, _collector: => SimpleCollector[T]): MetricMetadata[T] =
    new MetricMetadata(enabled, hostAddress, _collector)
}
