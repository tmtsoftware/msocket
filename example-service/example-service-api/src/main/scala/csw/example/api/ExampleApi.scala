package csw.example.api

import akka.stream.scaladsl.Source
import msocket.api.models.Subscription

import scala.concurrent.Future

trait ExampleApi {
  def hello(name: String): Future[String]
  def square(number: Int): Future[Int]

  def helloStream(name: String): Source[String, Subscription]
  def getNumbers(divisibleBy: Int): Source[Int, Subscription]
}
