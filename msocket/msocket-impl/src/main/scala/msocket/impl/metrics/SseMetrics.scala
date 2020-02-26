package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.HostDirectives.extractHost
import akka.stream.scaladsl.Source
import io.bullet.borer.Decoder
import io.prometheus.client.Gauge
import msocket.api.Labelled

import scala.concurrent.ExecutionContext

object SseMetrics extends SseMetrics

trait SseMetrics extends Metrics {

  private[metrics] val sseGaugeMetricName = "sse_active_request_total"
  def sseGauge(labelsNames: List[String]): Gauge =
    gauge(
      metricName = sseGaugeMetricName,
      help = "Total active sse connections",
      labelNames = labelsNames
    )

  def sseMetrics[Req: Decoder](req: Req, source: Source[ServerSentEvent, NotUsed], metricsEnabled: Boolean, gauge: => Gauge)(
      implicit labelGen: Req => Labelled[Req],
      ec: ExecutionContext
  ): Route =
    if (metricsEnabled) {
      extractHost { address =>
        val values: List[String] = labelValues(labelGen(req), address)
        val child                = gauge.labels(values: _*)
        child.inc()
        complete(onTermination(source, () => child.dec()))
      }
    } else complete(source)
}
