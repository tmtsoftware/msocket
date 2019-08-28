package akka.stream.scaladsl

trait Source[Out, Mat] {
  var onMessage: Out => Unit
  val mat: Mat
}
