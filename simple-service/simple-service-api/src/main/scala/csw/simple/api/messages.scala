package csw.simple.api

sealed trait StreamRequest

object StreamRequest {
  case class Square(number: Int)          extends StreamRequest
  case class GetNames(size: Int)          extends StreamRequest
  case class GetNumbers(divisibleBy: Int) extends StreamRequest
}

sealed trait PostRequest

object PostRequest {
  case class Hello(name: String)       extends PostRequest
  case class HelloStream(name: String) extends PostRequest
}

case class HelloStreamResponse(message: String)
