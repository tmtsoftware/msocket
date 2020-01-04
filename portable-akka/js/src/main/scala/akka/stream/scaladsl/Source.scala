package akka.stream.scaladsl

trait Source[Out, Mat] {
  val materializedValue: Mat
  def onMessage(handler: Out => Unit): Unit
  def onError(handler: Throwable => Unit): Unit
}
