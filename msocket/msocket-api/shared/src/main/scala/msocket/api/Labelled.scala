package msocket.api

import msocket.api.models.MetricLabels
import RequestMetadata._

trait LabelNames[T] {
  def get: List[String]
}

object LabelNames {
  def apply[T](implicit ev: LabelNames[T]): LabelNames[T] = ev

  def make[T](labelNames: String*): LabelNames[T] = new LabelNames[T] {
    override def get: List[String] = DefaultLabels ++ labelNames
  }

  implicit def defaultLabels[T]: LabelNames[T] = make()
}

case class RequestMetadata(address: String)
object RequestMetadata {
  val MsgLabel         = "msg"
  val HostAddressLabel = "hostname"
  val DefaultLabels    = List(MsgLabel, HostAddressLabel)
}

abstract class Labelled[T] {
  def labels(req: T, requestMetadata: RequestMetadata): MetricLabels
}

object Labelled {
  type Labels = Map[String, String]

  def apply[T: Labelled]: Labelled[T] = implicitly[Labelled[T]]

  def make[T: LabelNames](pf: PartialFunction[T, Labels]): Labelled[T] = make(pf.lift(_).getOrElse(Map.empty))

  implicit def emptyLabelled[T]: Labelled[T] = make(_ => Map.empty)

  private def make[T: LabelNames](labelsFactory: T => Labels): Labelled[T] = (req: T, requestMetadata: RequestMetadata) => {
    labelsFactory(req) ++ Map(MsgLabel -> createLabel(req), HostAddressLabel -> requestMetadata.address)
    MetricLabels(LabelNames[T].get, labelsFactory(req))
  }

  private def createLabel[A](obj: A): String = {
    val name = obj.getClass.getSimpleName
    if (name.endsWith("$")) name.dropRight(1) else name
  }
}
