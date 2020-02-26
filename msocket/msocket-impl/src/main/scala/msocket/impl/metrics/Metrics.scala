package msocket.impl.metrics

import akka.NotUsed
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives.{as, complete, entity, extractRequest}
import akka.http.scaladsl.server.Route
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
    extractRequest { httpRequest =>
      val hostAddress = httpRequest.uri.authority.host.address
      if (metricsEnabled)
        entity(as[Req]) { req =>
          val labels = labelGen(req).labels().withHost(hostAddress).labelValues
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
      val child = labelledGauge(reqF, gauge, hostAddress)
      child.map(_.inc())
      source.watchTermination() {
        case (mat, completion) =>
          completion.onComplete(_ => child.map(_.dec()))
          mat
      }
    } else source

  private def labelledGauge[T: Decoder](reqF: Future[T], gauge: => Gauge, hostAddress: String)(
      implicit ec: ExecutionContext,
      labelGen: T => Labelled[T]
  ): Future[Gauge.Child] = reqF.map { req =>
    val labelValues = labelGen(req).labels().withHost(hostAddress).labelValues
    val child       = gauge.labels(labelValues: _*)
    child
  }

  def sseMetrics[Req: Decoder](req: Req, source: Source[ServerSentEvent, NotUsed], metricsEnabled: Boolean, gauge: => Gauge)(
      implicit labelGen: Req => Labelled[Req],
      ec: ExecutionContext
  ): Route =
    if (metricsEnabled) {
      extractRequest { httpRequest =>
        val hostAddress = httpRequest.uri.authority.host.address

        val labelValues = labelGen(req).labels().withHost(hostAddress).labelValues
        val child       = gauge.labels(labelValues: _*)
        child.inc()
        complete {
          source
            .watchTermination() {
              case (mat, completion) =>
                completion.onComplete(_ => child.dec())
                mat
            }
        }
      }
    } else complete(source)
}
