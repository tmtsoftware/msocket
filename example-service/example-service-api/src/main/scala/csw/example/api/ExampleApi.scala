package csw.example.api

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

trait ExampleApi {
  def hello(name: String): Future[String]
  def helloStream(name: String): Source[String, NotUsed]

  def square(number: Int): Future[Int]
  def getNumbers(divisibleBy: Int): Source[Int, Future[Option[String]]]
}
