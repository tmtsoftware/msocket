package csw.example.api.protocol

/**
 * Transport agnostic error protocol is also defined as a Scala ADT
 * This ADT captures only the domain-specific error messages
 * Generic error messages are transported as [[msocket.api.models.ServiceError]]
 */
sealed abstract class ExampleError(msg: String) extends RuntimeException(msg)

object ExampleError {
  case class HelloError(count: Int)      extends ExampleError(s"current error count for hello call is $count")
  case class GetNumbersError(count: Int) extends ExampleError(s"current error count for get numbers call is $count")
}
