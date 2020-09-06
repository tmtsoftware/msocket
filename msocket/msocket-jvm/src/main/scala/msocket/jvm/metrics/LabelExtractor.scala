package msocket.jvm.metrics

trait LabelExtractor[Req] {
  def labelNames: List[String]
  def extract(request: Req): Map[String, String]

  def allLabelNames: List[String] = MetricCollector.DefaultLabels ++ labelNames
}

object LabelExtractor {
  def apply[Req](implicit x: LabelExtractor[Req]): LabelExtractor[Req] = x

  def make[Req](labelNames0: List[String])(labelsFactory0: PartialFunction[Req, Map[String, String]]): LabelExtractor[Req] =
    new LabelExtractor[Req] {
      override def labelNames: List[String]                   = labelNames0
      override def extract(request: Req): Map[String, String] = labelsFactory0.lift(request).getOrElse(Map.empty)
    }

  def empty[Req]: LabelExtractor[Req] = make(List.empty)(PartialFunction.empty)
}
