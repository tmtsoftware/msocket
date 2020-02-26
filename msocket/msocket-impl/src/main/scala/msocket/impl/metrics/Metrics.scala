package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.bullet.borer.Decoder
import io.prometheus.client.{CollectorRegistry, Counter, Gauge}
import msocket.api.Labelled

import scala.concurrent.ExecutionContext

object Metrics extends Metrics

trait Metrics {
  private[msocket] val prometheusRegistry: CollectorRegistry = PrometheusResponseTimeRecorder.DefaultRegistry

  val metricsRoute: Route = new MetricsEndpoint(prometheusRegistry).routes

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

  def withMetrics[Msg, Req: Decoder](
      source: Source[Msg, NotUsed],
      req: Req,
      metricsEnabled: Boolean,
      gauge: => Gauge,
      hostAddress: String
  )(implicit ec: ExecutionContext, labelGen: Req => Labelled[Req]): Source[Msg, NotUsed] =
    if (metricsEnabled) {
      val values = labelValues(labelGen(req), hostAddress)
      val child  = gauge.labels(values: _*)
      child.inc()
      source.watchTermination() {
        case (mat, completion) =>
          completion.onComplete(_ => child.dec())
          mat
      }
    } else source

  private[metrics] def labelValues[T](labelled: Labelled[T], address: String) =
    labelled.labels().withHost(address).values

}
