package csw.example.api.protocol

sealed abstract class ExampleError(msg: String) extends RuntimeException(msg)

object ExampleError {
  case class HelloError(count: Int)      extends ExampleError(s"current error count for hello call is $count")
  case class GetNumbersError(count: Int) extends ExampleError(s"current error count for get numbers call is $count")
}
