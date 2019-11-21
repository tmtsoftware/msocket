package csw.example.impl

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Source}
import csw.example.api.ExampleApi
import msocket.api.models.Subscription

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class ExampleImpl(implicit actorSystem: ActorSystem[_]) extends ExampleApi {
  import actorSystem.executionContext

  override def hello(name: String): Future[String] = {
    Future.successful(s"Hello $name")
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
    Source
      .fromIterator(() => Iterator.from(1).filter(_ % divisibleBy == 0))
      .throttle(1, 1.second)
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }
}
