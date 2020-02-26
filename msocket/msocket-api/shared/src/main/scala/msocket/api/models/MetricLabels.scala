package msocket.api.models

final case class MetricLabels(labelNames: List[String], private val labels: Map[String, String]) {
  import MetricLabels._

  def get(labelName: String): String = labels.getOrElse(labelName, "")

  def withHost(address: String): MetricLabels = copy(labels = labels + (HostAddressLabel -> address))

  def values: List[String] = labelNames.map(get)
}

object MetricLabels {
  val MsgLabel         = "msg"
  val HostAddressLabel = "hostname"
  val DefaultLabels    = List(MsgLabel, HostAddressLabel)
}
