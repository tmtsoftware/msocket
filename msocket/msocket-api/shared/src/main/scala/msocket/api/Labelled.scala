package msocket.api

import msocket.api.models.MetricLabels

trait LabelNames[T] {
  def get: List[String]
}

object LabelNames {
  def apply[T](implicit ev: LabelNames[T]): LabelNames[T] = ev

  def make[T](labelNames: String*): LabelNames[T] = new LabelNames[T] {
    override def get: List[String] = MetricLabels.DefaultLabels ++ labelNames
  }

  implicit def defaultLabels[T]: LabelNames[T] = make()
}

abstract class Labelled[T: LabelNames] {
  def labels(req: T): MetricLabels
}

object Labelled {
  type Labels = Map[String, String]

  def apply[T: Labelled]: Labelled[T] = implicitly[Labelled[T]]

  def make[T: LabelNames](pf: PartialFunction[T, Labels]): Labelled[T] = make(pf.lift(_).getOrElse(Map.empty))

  implicit def emptyLabelled[T]: Labelled[T] = make(_ => Map.empty)

  private def make[T: LabelNames](labelsFactory: T => Labels): Labelled[T] = new Labelled[T] {
    override def labels(req: T): MetricLabels = MetricLabels(LabelNames[T].get, labelsFactory(req))
  }
}
