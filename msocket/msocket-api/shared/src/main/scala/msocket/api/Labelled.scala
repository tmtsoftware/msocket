package msocket.api

import msocket.api.models.MetricLabels

import scala.reflect.ClassTag

trait Labelled[T] {
  def labels(): MetricLabels
}

object Labelled {
  def apply[T: Labelled]: Labelled[T] = implicitly[Labelled[T]]

  def make[T](obj: T): Labelled[T] = () => MetricLabels.msgLabel(obj)

  def withDefault[T](pf: PartialFunction[T, MetricLabels]): T => Labelled[T] =
    req => () => MetricLabels.msgLabel(req) + pf.lift(req).getOrElse(MetricLabels.empty)

  implicit def genericLabelled[T: ClassTag]: T => Labelled[T] = req => make(req)

}
