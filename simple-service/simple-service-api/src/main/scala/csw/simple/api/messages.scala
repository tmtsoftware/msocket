package csw.simple.api

sealed trait WebsocketRequest

object WebsocketRequest {
  case class Square(number: Int)          extends WebsocketRequest
  case class GetNames(size: Int)          extends WebsocketRequest
  case class GetNumbers(divisibleBy: Int) extends WebsocketRequest
}

sealed trait PostRequest

object PostRequest {
  case class Hello(name: String) extends PostRequest
}
