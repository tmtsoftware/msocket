package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives.{as, complete, entity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.HostDirectives.extractHost
import akka.stream.scaladsl.Source
import com.lonelyplanet.prometheus.PrometheusResponseTimeRecorder
import com.lonelyplanet.prometheus.api.MetricsEndpoint
import io.bullet.borer.Decoder
import io.prometheus.client.{CollectorRegistry, Counter, Gauge}
import msocket.api.{ErrorProtocol, Labelled}
import msocket.impl.post.ServerHttpCodecs._

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

  def routeMetrics[Req: Decoder: ErrorProtocol](metricsEnabled: Boolean, counter: => Counter)(
      handle: Req => Route
  )(implicit labelGen: Req => Labelled[Req]): Route =
    extractHost { address =>
      if (metricsEnabled)
        entity(as[Req]) { req =>
          val labels = labelValues(labelGen(req), address)
          counter.labels(labels: _*).inc()
          handle(req)
        } else entity(as[Req])(handle)
    }

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

  private def labelledGauge[T: Decoder](reqF: Future[T], gauge: => Gauge, hostAddress: String)(
      implicit ec: ExecutionContext,
      labelGen: T => Labelled[T]
  ): Future[Gauge.Child] = reqF.map { req =>
    val values = labelValues(labelGen(req), hostAddress)
    gauge.labels(values: _*)
  }

  private def onTermination[T](source: Source[T, NotUsed], onCompletion: () => Unit)(implicit ec: ExecutionContext) =
    source.watchTermination() {
      case (mat, completion) =>
        completion.onComplete(_ => onCompletion())
        mat
    }

  private def labelValues[T](labelled: Labelled[T], address: String) =
    labelled.labels().withHost(address).labelValues

}
