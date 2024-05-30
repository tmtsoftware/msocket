package msocket.jvm.metrics

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import io.prometheus.client.{Counter, Gauge}

import scala.concurrent.ExecutionContext

class MetricCollector[Req: LabelExtractor](
    val enabled: Boolean,
    val request: Req,
    val appName: Option[String],
    val username: Option[String],
    _counter: => Option[Counter],
    _gauge: => Option[Gauge]
)(implicit ec: ExecutionContext) {

  import MetricCollector._

  private lazy val labels: Seq[String] = {
    val labelMap = LabelExtractor[Req].extract(request) ++ Map(
      MsgLabel      -> LabelExtractor.createLabel(request),
      AppNameLabel  -> appName.getOrElse("unknown"),
      UsernameLabel -> username.getOrElse("unknown")
    )
    LabelExtractor[Req].allLabelNames.map(name => labelMap.getOrElse(name, ""))
  }

  private lazy val counter: Option[Counter.Child] = _counter.map(_.labels(labels*))
  private lazy val gauge: Option[Gauge.Child]     = _gauge.map(_.labels(labels*))

  def incGauge(): Unit   = gauge.foreach(_.inc())
  def decGauge(): Unit   = gauge.foreach(_.dec())
  def incCounter(): Unit = counter.foreach(_.inc())

  def streamMetric[Msg](source: Source[Msg, NotUsed]): Source[Msg, NotUsed] = {
    if (enabled) {
      incGauge()
      source
        .wireTap { _ => incCounter() }
        .watchTermination() { case (mat, completion) =>
          completion.onComplete(_ => decGauge())
          mat
        }
    } else source
  }

  def record(): Unit = {
    if (enabled) incCounter()
  }
}

object MetricCollector {
  val MsgLabel      = "msg"
  val AppNameLabel  = "app_name"
  val UsernameLabel = "username"
  val DefaultLabels = List(MsgLabel, AppNameLabel, UsernameLabel)
}
