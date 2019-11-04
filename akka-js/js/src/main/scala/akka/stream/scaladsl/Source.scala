package akka.stream.scaladsl

trait Source[Out, Mat] {
  val materializedValue: Mat
  def runForeach(f: Out => Unit): Unit
}
