package msocket.api

import msocket.api.models.MetricLabels

trait LabelNames[T] {
  def names(): List[String]

  // always use this internally
  final def get: List[String] = (MetricLabels.DefaultLabels ++ names()).distinct
}

object LabelNames {
  def apply[T](implicit ev: LabelNames[T]): LabelNames[T] = ev
  def make[T](labelNames: String*): LabelNames[T]         = () => labelNames.toList

  implicit def defaultLabels[T]: LabelNames[T] = make()
}

abstract class Labelled[T: LabelNames] {
  def labels(): MetricLabels
}

object Labelled {
  type Labels = Map[String, String]

  def apply[T: Labelled]: Labelled[T] = implicitly[Labelled[T]]

  def make[T: LabelNames](pf: PartialFunction[T, Labels]): T => Labelled[T] = make(pf.lift(_).getOrElse(Map.empty))

  implicit def emptyLabelled[T]: T => Labelled[T] = make(_ => Map.empty)

  private def make[Req](labelsFactory: Req => Labels): Req => Labelled[Req] =
    req =>
      new Labelled[Req] {
        override def labels(): MetricLabels = MetricLabels(LabelNames[Req].get, labelsFactory(req))
      }
}
