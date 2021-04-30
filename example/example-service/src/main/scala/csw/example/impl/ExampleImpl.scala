package csw.example.impl

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleError.{GetNumbersError, HelloError}
import csw.example.api.models.Bag
import msocket.jvm.SourceExtension.RichSource
import msocket.api.Subscription

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.util.Random

/**
 * Implementation for the APIs defined in [[ExampleApi]]
 */
class ExampleImpl(implicit actorSystem: ActorSystem[_]) extends ExampleApi {
  import actorSystem.executionContext

  override def hello(name: String): Future[String] = {
    name match {
      case "idiot" => Future.failed(HelloError(5))                                  //domain error
      case "fool"  => Future.failed(new IllegalArgumentException("you are a fool")) //generic error
      case x       => Future.successful(s"Hello $x")
    }
  }

  override def square(number: Int): Future[Int] = {
    // this is to simulate a call which takes much longer (say 3 min) than the implicit timeout of the transport (say 2 min)
    akka.pattern.after(3.minutes, actorSystem.toClassic.scheduler) {
      Future.successful(number * number)
    }
  }

  //////////////

  override def helloStream(name: String): Source[String, Subscription] = {
    Source
      .tick(100.millis, 100.millis, ())
      .scan(0)((acc, _) => acc + 1)
      .map(x => s"hello \n $name again $x")
      .mapMaterializedValue(_ => NotUsed)
      .take(50)
      .withSubscription()
  }

  override def getNumbers(divisibleBy: Int): Source[Int, Subscription] = {
    val stream = if (divisibleBy == -1) {
      Source.failed(GetNumbersError(17))
    } else {
      Source
        .fromIterator(() => Iterator.from(1).filter(_ % divisibleBy == 0))
        .throttle(1, 1.second)
    }

    stream.withSubscription()
  }

  override def randomBag(): Future[Bag] = Future.successful(randomize())

  override def randomBagStream(): Source[Bag, Subscription] = {
    Source
      .fromIterator(() => Iterator.continually(randomize()))
      .throttle(1, 1.second)
      .withSubscription()
  }

  private def randomize() = {
    val random = new Random()
    Bag(
      red = random.between(1, 10),
      green = random.between(1, 10),
      blue = random.between(1, 10)
    )
  }
}
