package csw.simple.impl

import akka.stream.DelayOverflowStrategy
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import csw.simple.api.SimpleApi

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.util.Random

class SimpleImpl extends SimpleApi {
  /////////////////
  override def hello(name: String): Future[String] = {
    Future.successful(s"Hello $name")
  }
  override def square(number: Int): Future[Int] = {
    Future.successful(number * number)
  }

  ///////////////////
  override def getNames(size: Int): Source[String, NotUsed] = {
    Source
      .tick(1.second, 1.second, ())
      .map(_ => Random.alphanumeric.take(size).mkString)
      .mapMaterializedValue(_ => NotUsed)
  }
  override def getNumbers(divisibleBy: Int): Source[Int, NotUsed] = {
    Source
      .fromIterator(() => Iterator.from(1).filter(_ % divisibleBy == 0))
      .delay(1.second, DelayOverflowStrategy.backpressure)
  }

  /////////////////
  override def helloAll(names: Source[String, NotUsed]): Source[String, NotUsed] = {
    names.mapAsync(1)(hello)
  }
  override def squareAll(numbers: Source[Int, NotUsed]): Source[Int, NotUsed] = {
    numbers.mapAsync(1)(square)
  }

  /////////////////
  override def ping(msg: String): Future[Done] = Future.successful {
    println(s"received ping: $msg")
    Done
  }
  override def publish(number: Int): Future[Done] = Future.successful {
    println(s"published $number")
    Done
  }
}
