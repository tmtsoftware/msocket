package csw.example.api.client

import akka.stream.scaladsl.Source
import csw.example.api._
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.api.Transport
import msocket.api.models.Subscription

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class ExampleClient(transport: Transport[ExampleRequest]) extends ExampleApi with Codecs {
  override def hello(name: String): Future[String] = transport.requestResponse[String](Hello(name))
  override def square(number: Int): Future[Int]    = transport.requestResponse[Int](Square(number), 10.minutes)

  override def helloStream(name: String): Source[String, Subscription] = transport.requestStream[String](HelloStream(name))
  override def getNumbers(divisibleBy: Int): Source[Int, Subscription] = transport.requestStream[Int](GetNumbers(divisibleBy))
}
