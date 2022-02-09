package experiments

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.{ExecutionContext, Future}

object SampleActor {

  trait Msg
  case object Increment            extends Msg
  case object IncrementViaCallback extends Msg
  case object GetTotal             extends Msg

  def behavior(implicit ec: ExecutionContext): Behavior[Msg] = {
    var total = 0

    Behaviors.receiveMessage {
      case Increment            =>
        total += 1
        Behaviors.same
      case IncrementViaCallback =>
        Future.unit.foreach { _ =>
          total += 1
        }
        Behaviors.same
      case GetTotal             =>
        println(total)
        Behaviors.same
    }
  }
}
