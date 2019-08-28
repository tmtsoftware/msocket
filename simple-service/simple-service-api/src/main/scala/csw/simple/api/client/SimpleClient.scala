package csw.simple.api.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.simple.api.PostRequest.{Hello, HelloStream}
import csw.simple.api.StreamRequest._
import csw.simple.api._
import msocket.api.RequestClient

import scala.concurrent.Future

class SimpleClient(websocketClient: RequestClient[StreamRequest], postClient: RequestClient[PostRequest]) extends SimpleApi with Codecs {
  override def hello(name: String): Future[String] = postClient.requestResponse[String](Hello(name))
  override def helloStream(name: String): Source[HelloStreamResponse, NotUsed] =
    postClient.requestStream[HelloStreamResponse](HelloStream(name))

  override def square(number: Int): Future[Int] = websocketClient.requestResponse[Int](Square(number))

  override def getNames(size: Int): Source[String, NotUsed] = websocketClient.requestStream[String](GetNames(size))

  override def getNumbers(divisibleBy: Int): Source[Int, Future[Option[String]]] = {
    websocketClient.requestStreamWithError[Int, String](GetNumbers(divisibleBy))
  }
}
