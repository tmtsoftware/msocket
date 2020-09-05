package msocket.impl.metrics

import akka.http.scaladsl.model.RemoteAddress
import io.prometheus.client.{Counter, Gauge}
import msocket.service.{Labelled, RequestMetadata}

import scala.concurrent.ExecutionContext

class MetricCollector[Req: Labelled](
    val enabled: Boolean,
    val request: Req,
    _appName: Option[String],
    _counter: => Option[Counter],
    _gauge: => Option[Gauge],
    private val clientAddress: RemoteAddress
)(implicit val ec: ExecutionContext) {
  private val clientIp: String                    = clientAddress.toString()
  private val appName: String                     = _appName.getOrElse("unknown")
  private lazy val labels: Seq[String]            = Labelled[Req].labels(request, RequestMetadata(clientIp, appName)).values
  private lazy val counter: Option[Counter.Child] = _counter.map(_.labels(labels: _*))
  private lazy val gauge: Option[Gauge.Child]     = _gauge.map(_.labels(labels: _*))

  def incGauge(): Unit   = gauge.foreach(_.inc())
  def decGauge(): Unit   = gauge.foreach(_.dec())
  def incCounter(): Unit = counter.foreach(_.inc())
}
