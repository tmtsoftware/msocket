package msocket.api

import msocket.api.models.MetricLabels
import msocket.api.models.MetricLabels.MsgLabel

import scala.reflect.ClassTag

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
  def apply[T: Labelled]: Labelled[T] = implicitly[Labelled[T]]

  def make[T: LabelNames](obj: T): Labelled[T] = new Labelled[T] {
    override def labels(): MetricLabels = MetricLabels(LabelNames[T].get, msgLabel(obj))
  }

  type Labels = Map[String, String]
  def withDefault[T: LabelNames](pf: PartialFunction[T, Labels]): T => Labelled[T] =
    req =>
      new Labelled[T] {
        override def labels(): MetricLabels = MetricLabels(LabelNames[T].get, msgLabel(req) ++ pf.lift(req).getOrElse(Map.empty))
      }

  implicit def genericLabelled[T: ClassTag]: T => Labelled[T] = req => make(req)

  private def msgLabel[T](obj: T): Map[String, String] = Map(MsgLabel -> createLabel(obj))

  private def createLabel[A](obj: A): String = {
    val name = obj.getClass.getSimpleName
    if (name.endsWith("$")) name.dropRight(1) else name
  }
}
