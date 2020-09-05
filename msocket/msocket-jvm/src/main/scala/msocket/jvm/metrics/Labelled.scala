package msocket.jvm.metrics

import msocket.jvm.metrics.RequestMetadata._

case class RequestMetadata(address: String, appName: String)
object RequestMetadata {
  val MsgLabel         = "msg"
  val HostAddressLabel = "hostname"
  val AppNameLabel     = "app_name"
  val DefaultLabels    = List(MsgLabel, HostAddressLabel, AppNameLabel)
}

sealed abstract class Labelled[T] {
  def labelNames: List[String]
  def labels(req: T, requestMetadata: RequestMetadata): MetricLabels
}

object Labelled {
  type Labels = Map[String, String]

  def apply[T: Labelled]: Labelled[T] = implicitly[Labelled[T]]

  def make[T](labelNames: List[String])(labelsFactory: PartialFunction[T, Labels]): Labelled[T] =
    make(labelNames, labelsFactory.lift(_).getOrElse(Map.empty))

  implicit def emptyLabelled[T]: Labelled[T] = make(List.empty, _ => Map.empty)

  private def make[T](labelList: List[String], labelsFactory: T => Labels): Labelled[T] =
    new Labelled[T] {
      override def labelNames: List[String] = DefaultLabels ++ labelList

      override def labels(req: T, requestMetadata: RequestMetadata): MetricLabels = {
        val labelMap = labelsFactory(req) ++ Map(
          MsgLabel         -> createLabel(req),
          HostAddressLabel -> requestMetadata.address,
          AppNameLabel     -> requestMetadata.appName
        )
        MetricLabels(labelNames, labelMap)
      }
    }

  def createLabel[A](obj: A): String = {
    val name = obj.getClass.getSimpleName
    if (name.endsWith("$")) name.dropRight(1) else name
  }
}
