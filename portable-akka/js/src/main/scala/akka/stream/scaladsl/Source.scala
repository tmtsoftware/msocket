package akka.stream.scaladsl

trait Source[Out, Mat] {
  val materializedValue: Mat
  def foreach(f: Out => Unit): Unit
}
