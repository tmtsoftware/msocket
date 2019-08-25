package csw.simple.impl

import akka.NotUsed
import akka.stream.DelayOverflowStrategy
import akka.stream.scaladsl.Source
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

  override def getNumbers(divisibleBy: Int): Source[Int, Future[Option[String]]] = {
    if (divisibleBy == 0) {
      Source.empty[Int].mapMaterializedValue(_ => Future.successful(Some("divide by zero error")))
    } else {
      Source
        .fromIterator(() => Iterator.from(1).filter(_ % divisibleBy == 0))
        .delay(1.second, DelayOverflowStrategy.backpressure)
        .mapMaterializedValue(_ => Future.successful(None))
    }
  }

}
