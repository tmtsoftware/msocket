package csw.simple.api

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

trait SimpleApi {
  def hello(name: String): Future[String]
  def square(number: Int): Future[Int]

  def getNames(size: Int): Source[String, NotUsed]
  def getNumbers(divisibleBy: Int): Source[Int, NotUsed]
}
