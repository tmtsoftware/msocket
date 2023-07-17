package experiments

import org.apache.pekko.actor.typed.*
import experiments.BehaviourExtensions.withSafeEc

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import SampleActor.*

object SafeEcDemo {
  def main(args: Array[String]): Unit = {
    val demo = ActorSystem(
//      behavior(global),
      withSafeEc(ec => behavior(ec)),
      "demo"
    )

    (1 to 100000).foreach { _ =>
      Future {
        demo ! Increment
      }
    }

    (1 to 100000).foreach { _ =>
      Future {
        demo ! IncrementViaCallback
      }
    }

    Thread.sleep(2000)
    demo ! GetTotal
  }
}
