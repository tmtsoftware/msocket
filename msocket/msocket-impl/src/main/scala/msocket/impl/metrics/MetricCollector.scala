package msocket.impl.metrics

import akka.http.scaladsl.model.RemoteAddress
import akka.http.scaladsl.server.RequestContext
import io.prometheus.client.{Counter, Gauge}
import msocket.api.{Labelled, RequestMetadata}

import scala.concurrent.ExecutionContext

class MetricCollector[Req: Labelled](
    val enabled: Boolean,
    val request: Req,
    _counter: => Option[Counter],
    _gauge: => Option[Gauge],
    private val clientAddress: RemoteAddress,
    requestContext: RequestContext
) {
  private val AppNameHeader = "App-Name"

  implicit lazy val ec: ExecutionContext = requestContext.executionContext

  private val clientIp: String = clientAddress.toString()
  private val appName: String =
    requestContext.request.headers.find(_.name.equalsIgnoreCase(AppNameHeader)).map(_.value).getOrElse("unknown")

  private lazy val labels: Seq[String]            = Labelled[Req].labels(request, RequestMetadata(clientIp, appName)).values
  private lazy val counter: Option[Counter.Child] = _counter.map(_.labels(labels: _*))
  private lazy val gauge: Option[Gauge.Child]     = _gauge.map(_.labels(labels: _*))

  def incGauge(): Unit = gauge.foreach(_.inc())
  def decGauge(): Unit = gauge.foreach(_.dec())

  def incCounter(): Unit = counter.foreach(_.inc())
}
