package msocket.api.models

final case class MetricLabels(labelNames: List[String], private val labels: Map[String, String]) {
  import MetricLabels._

  def get(labelName: String): String = labels.getOrElse(labelName, "")

  def withMandatoryLabels[T](msg: T, address: String): MetricLabels =
    copy(labels = labels ++ Map(MsgLabel -> createLabel(msg), HostAddressLabel -> address))

  def values: List[String] = labelNames.map(get)

  private def createLabel[A](obj: A): String = {
    val name = obj.getClass.getSimpleName
    if (name.endsWith("$")) name.dropRight(1) else name
  }
}

object MetricLabels {
  val MsgLabel         = "msg"
  val HostAddressLabel = "hostname"
  val DefaultLabels    = List(MsgLabel, HostAddressLabel)
}
