package csw.example.api.client

import akka.stream.scaladsl.Source
import csw.example.api._
import csw.example.api.protocol.ExampleProtocol._
import csw.example.api.models.Bag
import msocket.api.{Subscription, Transport}

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

/**
 * Assuming that an appropriate transport instance is provided at the client-app site, we can mechanically derive the client for the
 * [[ExampleApi]] by delegating to correct interaction models (e.g., requestResponse or requestStream). See the docs for [[Transport]]
 */
class ExampleClient(responseTransport: Transport[ExampleRequest], streamTransport: Transport[ExampleStreamRequest]) extends ExampleApi {

  override def hello(name: String): Future[String] = responseTransport.requestResponse[String](Hello(name))
  override def randomBag(): Future[Bag]            = responseTransport.requestResponse[Bag](RandomBag)

  override def square(number: Int): Future[Int]                        = streamTransport.requestResponse[Int](Square(number), 10.minutes)
  override def helloStream(name: String): Source[String, Subscription] = streamTransport.requestStream[String](HelloStream(name))
  override def getNumbers(divisibleBy: Int): Source[Int, Subscription] = streamTransport.requestStream[Int](GetNumbers(divisibleBy))
  override def randomBagStream(): Source[Bag, Subscription]            = streamTransport.requestStream[Bag](RandomBagStream)
}
