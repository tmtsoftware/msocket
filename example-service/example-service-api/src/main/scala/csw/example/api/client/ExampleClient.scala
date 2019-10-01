package csw.example.api.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.example.api._
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.api.{StreamStatus, Transport}

import scala.concurrent.Future

class ExampleClient(transport: Transport[ExampleRequest]) extends ExampleApi with Codecs {
  override def hello(name: String): Future[String] = transport.requestResponse[String](Hello(name))
  override def square(number: Int): Future[Int]    = transport.requestResponseWithDelay[Int](Square(number))

  override def helloStream(name: String): Source[String, NotUsed] = transport.requestStream[String](HelloStream(name))
  override def getNumbers(divisibleBy: Int): Source[Int, Future[StreamStatus]] = {
    transport.requestStreamWithStatus[Int](GetNumbers(divisibleBy))
  }
}
