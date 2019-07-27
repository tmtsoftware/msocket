package csw.simple.api

import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}

import scala.concurrent.Future

trait SimpleApi {
  def hello(name: String): Future[String]
  def square(number: Int): Future[Int]

  def getNames(size: Int): Source[String, NotUsed]
  def getNumbers(divisibleBy: Int): Source[Int, NotUsed]

  def helloAll(names: Source[String, NotUsed]): Source[String, NotUsed]
  def squareAll(numbers: Source[Int, NotUsed]): Source[Int, NotUsed]

  def ping(msg: String): Future[Done]
  def publish(number: Int): Future[Done]
}
