package msocket.api

import msocket.api.models.MetricLabels

import scala.reflect.ClassTag

trait Labellable[T] {
  def metricLabels(): MetricLabels
}

object Labellable {
  def apply[T: Labellable]: Labellable[T] = implicitly[Labellable[T]]

  def make[T: ClassTag](labels: Map[String, String]): Labellable[T] = () => MetricLabels(labels)
  implicit def genericLabellable[T: ClassTag]: T => Labellable[T]   = req => make(Map("msg" -> req.getClass.getSimpleName))
}
