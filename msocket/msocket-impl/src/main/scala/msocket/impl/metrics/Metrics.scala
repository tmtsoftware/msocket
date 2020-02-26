package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.bullet.borer.Decoder
import io.prometheus.client.{CollectorRegistry, Counter, Gauge}
import msocket.api.Labelled

import scala.concurrent.{ExecutionContext, Future}

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

  private[metrics] def labelledGauge[T: Decoder](reqF: Future[T], gauge: => Gauge, hostAddress: String)(
      implicit ec: ExecutionContext,
      labelGen: T => Labelled[T]
  ): Future[Gauge.Child] = reqF.map { req =>
    val values = labelValues(labelGen(req), hostAddress)
    gauge.labels(values: _*)
  }

  private[metrics] def onTermination[T](source: Source[T, NotUsed], onCompletion: () => Unit)(implicit ec: ExecutionContext) =
    source.watchTermination() {
      case (mat, completion) =>
        completion.onComplete(_ => onCompletion())
        mat
    }

  private[metrics] def labelValues[T](labelled: Labelled[T], address: String) =
    labelled.labels().withHost(address).labelValues

}
