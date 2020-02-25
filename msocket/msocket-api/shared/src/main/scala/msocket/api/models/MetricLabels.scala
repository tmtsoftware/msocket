package msocket.api.models

final case class MetricLabels(labels: Map[String, String]) extends AnyVal {
  import MetricLabels._

  def get(labelName: String): String = labels.getOrElse(labelName, "")

  def withHost(address: String): MetricLabels = copy(labels + (HostAddressLabel -> address))

  def +(that: MetricLabels): MetricLabels = MetricLabels(this.labels ++ that.labels)

  def labelValues: List[String] =
    get(MsgLabel) :: get(HostAddressLabel) :: labels.removedAll(List(MsgLabel, HostAddressLabel)).values.toList
}

object MetricLabels {
  val MsgLabel         = "msg"
  val HostAddressLabel = "hostname"
  val DefaultLabels    = List(MsgLabel, HostAddressLabel)

  val empty: MetricLabels = MetricLabels(Map.empty)

  def msgLabel[T](obj: T): MetricLabels = MetricLabels(Map(MsgLabel -> createLabel(obj)))

  private def createLabel[A](obj: A): String = {
    val name = obj.getClass.getSimpleName
    if (name.endsWith("$")) name.dropRight(1) else name
  }
}
