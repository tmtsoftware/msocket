package csw.example.api

import akka.stream.scaladsl.Source
import csw.example.model.Bag
import msocket.api.Subscription

import scala.concurrent.Future

/**
 * We begin by defining an interface that will be the contract between service and the client
 * It has a mix of requestResponse methods (e.g. hello) that return a [[Future]]
 * and requestStream style methods (e.g. getNumbers) that returns a [[Source]]
 * with Subscription as its materialized value
 */
trait ExampleApi {
  // these are requestResponse style APIs that are only supported by transports that have implicit timeouts
  def hello(name: String): Future[String]
  def juggle(): Future[Bag]

  // this looks like requestResponse style API but is implemented on top of streaming API with explicit timeout
  // because that timeout could be much larger than the implicit timeout of the transport
  def square(number: Int): Future[Int]

  // these are requestStream style APIs
  def helloStream(name: String): Source[String, Subscription]
  def getNumbers(divisibleBy: Int): Source[Int, Subscription]
  def juggleStream(): Source[Bag, Subscription]
}
