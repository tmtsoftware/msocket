package csw.example.impl

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Source}
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleError.{GetNumbersError, HelloError}
import msocket.api.Subscription

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class ExampleImpl(implicit actorSystem: ActorSystem[_]) extends ExampleApi {
  import actorSystem.executionContext

  override def hello(name: String): Future[String] = {
    name match {
      case "idiot" => Future.failed(HelloError(5))
      case "fool"  => Future.failed(new IllegalArgumentException("you are a fool"))
      case x       => Future.successful(s"Hello $x")
    }
  }

  override def square(number: Int): Future[Int] = {
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
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }

  override def getNumbers(divisibleBy: Int): Source[Int, Subscription] = {
    val stream = if (divisibleBy == -1) {
      Source.failed(GetNumbersError(17))
    } else {
      Source
        .fromIterator(() => Iterator.from(1).filter(_ % divisibleBy == 0))
        .throttle(1, 1.second)
    }

    stream
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }
}
