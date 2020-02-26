package msocket.impl.metrics

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Decoder
import io.prometheus.client.Gauge
import msocket.api.Labelled

import scala.concurrent.{ExecutionContext, Future}

object WebsocketMetrics extends WebsocketMetrics

trait WebsocketMetrics extends Metrics {

  private[metrics] val websocketGaugeMetricName = "websocket_active_request_total"
  def websocketGauge(labelsNames: List[String]): Gauge =
    gauge(
      metricName = websocketGaugeMetricName,
      help = "Total active websocket connections",
      labelNames = labelsNames
    )

  def wsMetrics[Msg, T: Decoder](
      source: Source[Msg, NotUsed],
      reqF: Future[T],
      metricsEnabled: Boolean,
      gauge: => Gauge,
      hostAddress: String
  )(implicit ec: ExecutionContext, labelGen: T => Labelled[T]): Source[Msg, NotUsed] =
    if (metricsEnabled) {
      val childF = labelledGauge(reqF, gauge, hostAddress)
      childF.map(_.inc())

      onTermination(source, () => childF.map(_.dec()))
    } else source
}
