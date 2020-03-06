package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.server.Directives.extractClientIP
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.scaladsl.Source
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.prometheus.client.{Counter, Gauge}
import msocket.api.{Labelled, RequestMetadata}

object Metrics extends Metrics

trait Metrics {
  private lazy val prometheusRegistry = PrometheusResponseTimeRecorder.DefaultRegistry
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
      req: Req,
      metadata: MetricMetadata
  ): Source[Msg, NotUsed] = {
    import metadata._
    if (enabled) {
      val values = Labelled[Req].labels(req, RequestMetadata(clientIp)).values
      incGauge(values)
      val perMsgCounter = metricsCounter.map(_.labels(values: _*))

      source
        .map { msg =>
          perMsgCounter.foreach(_.inc())
          msg
        }
        .watchTermination() {
          case (mat, completion) =>
            completion.onComplete(_ => decGauge(values))
            mat
        }
    } else source
  }

  def withMetricMetadata[T](
      enabled: Boolean,
      counter: => Option[Counter] = None,
      gauge: => Option[Gauge] = None
  ): Directive1[MetricMetadata] =
    extractClientIP.flatMap { clientIp =>
      extract(new MetricMetadata(enabled, counter, gauge, clientIp, _))
    }

}
