package akka.stream.scaladsl

trait Source[Out, Mat] {
  val materializedValue: Mat
  def subscribe(f: Out => Unit): Unit
}
