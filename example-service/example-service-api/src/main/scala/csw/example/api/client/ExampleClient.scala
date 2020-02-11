package csw.example.api.client

import akka.stream.scaladsl.Source
import csw.example.api._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest._
import csw.example.model.Bag
import msocket.api.{Subscription, Transport}

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

/**
 * Assuming that an appropriate transport instance is provided at the client-app site,
 * we can mechanically derive the client for the [[ExampleApi]] by delegating to correct
 * interaction models (e.g., requestResponse or requestStream). See the docs for [[Transport]]
 */
class ExampleClient(transport: Transport[ExampleRequest]) extends ExampleApi {
  override def hello(name: String): Future[String] = transport.requestResponse[String](Hello(name))
  override def square(number: Int): Future[Int]    = transport.requestResponse[Int](Square(number), 10.minutes)

  override def helloStream(name: String): Source[String, Subscription] = transport.requestStream[String](HelloStream(name))
  override def getNumbers(divisibleBy: Int): Source[Int, Subscription] = transport.requestStream[Int](GetNumbers(divisibleBy))

  override def juggle(bag: Bag): Future[Bag]                     = transport.requestResponse[Bag](Juggle(bag))
  override def juggleStream(bag: Bag): Source[Bag, Subscription] = transport.requestStream[Bag](JuggleStream(bag))
}
