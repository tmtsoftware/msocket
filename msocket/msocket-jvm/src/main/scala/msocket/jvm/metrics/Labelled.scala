package msocket.jvm.metrics

import msocket.jvm.metrics.RequestMetadata._

case class RequestMetadata(address: String, appName: String)
object RequestMetadata {
  val MsgLabel         = "msg"
  val HostAddressLabel = "hostname"
  val AppNameLabel     = "app_name"
  val DefaultLabels    = List(MsgLabel, HostAddressLabel, AppNameLabel)
}

sealed abstract class Labelled[Req] {
  def labelNames: List[String]
  def labels(req: Req, requestMetadata: RequestMetadata): MetricLabels
}

object Labelled {
  type Labels = Map[String, String]

  def apply[Req: Labelled]: Labelled[Req] = implicitly[Labelled[Req]]

  def make[Req](labelNames: List[String])(labelsFactory: PartialFunction[Req, Labels]): Labelled[Req] =
    make(labelNames, labelsFactory.lift(_).getOrElse(Map.empty))

  implicit def emptyLabelled[Req]: Labelled[Req] = make(List.empty, _ => Map.empty)

  private def make[Req](labelList: List[String], labelsFactory: Req => Labels): Labelled[Req] =
    new Labelled[Req] {
      override def labelNames: List[String] = DefaultLabels ++ labelList

      override def labels(req: Req, requestMetadata: RequestMetadata): MetricLabels = {
        val labelMap = labelsFactory(req) ++ Map(
          MsgLabel         -> createLabel(req),
          HostAddressLabel -> requestMetadata.address,
          AppNameLabel     -> requestMetadata.appName
        )
        MetricLabels(labelNames, labelMap)
      }
    }

  def createLabel[Req](obj: Req): String = {
    val name = obj.getClass.getSimpleName
    if (name.endsWith("$")) name.dropRight(1) else name
  }
}
