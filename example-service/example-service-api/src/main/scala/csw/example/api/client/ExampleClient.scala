package csw.example.api.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.example.api._
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.api.RequestClient

import scala.concurrent.Future

class ExampleClient(postClient: RequestClient[ExampleRequest]) extends ExampleApi with Codecs {
  override def hello(name: String): Future[String] = postClient.requestResponse[String](Hello(name))
  override def square(number: Int): Future[Int]    = postClient.requestResponseWithDelay[Int](Square(number))

  override def helloStream(name: String): Source[String, NotUsed] = postClient.requestStream[String](HelloStream(name))
  override def getNumbers(divisibleBy: Int): Source[Int, Future[Option[String]]] = {
    postClient.requestStreamWithError[Int, String](GetNumbers(divisibleBy))
  }
}
