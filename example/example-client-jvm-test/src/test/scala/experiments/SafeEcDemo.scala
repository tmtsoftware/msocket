package experiments

import akka.actor.typed.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SafeEcDemo {

  def main(args: Array[String]): Unit = {
    import SampleActor.*
    val demo = ActorSystem(safeBehavior, "demo")

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
