package msocket.api.models

final case class MetricLabels(labelNames: List[String], private val labels: Map[String, String]) {
  def get(labelName: String): String = labels.getOrElse(labelName, "")
  def values: List[String]           = labelNames.map(get)
}
