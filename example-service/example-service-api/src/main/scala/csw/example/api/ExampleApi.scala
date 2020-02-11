package csw.example.api

import akka.stream.scaladsl.Source
import csw.example.model.Bag
import msocket.api.Subscription

import scala.concurrent.Future

trait ExampleApi {
  def hello(name: String): Future[String]
  def square(number: Int): Future[Int]
  def juggle(bag: Bag): Future[Bag]

  def helloStream(name: String): Source[String, Subscription]
  def getNumbers(divisibleBy: Int): Source[Int, Subscription]
  def juggleStream(bag: Bag): Source[Bag, Subscription]
}
