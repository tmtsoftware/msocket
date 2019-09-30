package csw.example.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.api.StreamStatus

import scala.concurrent.Future

trait ExampleApi {
  def hello(name: String): Future[String]
  def square(number: Int): Future[Int]

  def helloStream(name: String): Source[String, NotUsed]
  def getNumbers(divisibleBy: Int): Source[Int, Future[StreamStatus]]
}
