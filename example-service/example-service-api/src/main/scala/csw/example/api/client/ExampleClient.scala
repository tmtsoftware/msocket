package csw.example.api.client

import akka.stream.scaladsl.Source
import csw.example.api._
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Juggle, JuggleStream, Square}
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import csw.example.model.Bag
import msocket.api.{Subscription, Transport}

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class ExampleClient(transport: Transport[ExampleRequest]) extends ExampleApi with ExampleCodecs {
  override def hello(name: String): Future[String] = transport.requestResponse[String](Hello(name))
  override def square(number: Int): Future[Int]    = transport.requestResponse[Int](Square(number), 10.minutes)

  override def helloStream(name: String): Source[String, Subscription] = transport.requestStream[String](HelloStream(name))
  override def getNumbers(divisibleBy: Int): Source[Int, Subscription] = transport.requestStream[Int](GetNumbers(divisibleBy))

  override def juggle(bag: Bag): Future[Bag] = transport.requestResponse[Bag](Juggle(bag))
  override def juggleStream(bag: Bag): Source[Bag, Subscription] = transport.requestStream[Bag](JuggleStream(bag))
}
