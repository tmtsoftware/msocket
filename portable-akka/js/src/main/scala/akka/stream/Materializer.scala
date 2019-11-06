package akka.stream

abstract class Materializer

object Materializer {
  object Implicits {
    implicit val Dummy: Materializer = null
  }
}
