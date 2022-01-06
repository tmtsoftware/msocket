package experiments

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

object BehaviourExtensions {

  class RunnableInterceptor[T: ClassTag] extends BehaviorInterceptor[Any, T] {
    override def aroundReceive(ctx: TypedActorContext[Any], msg: Any, target: BehaviorInterceptor.ReceiveTarget[T]): Behavior[T] =
      msg match {
        case x: T        => target(ctx, x)
        case x: Runnable => x.run(); Behaviors.same
        case _           => Behaviors.unhandled
      }
  }

  def withActorRef[T: ClassTag](factory: ActorRef[Runnable] => Behavior[T]): Behavior[T] = {
    Behaviors
      .setup[Any] { ctx =>
        Behaviors.intercept[Any, T](() => new RunnableInterceptor[T])(factory(ctx.self))
      }
      .narrow
  }

  def withSafeEc[T: ClassTag](factory: ExecutionContext => Behavior[T]): Behavior[T] = {
    withActorRef[T] { actorRef =>
      val ec = new ExecutionContext {
        override def execute(runnable: Runnable): Unit     = actorRef ! runnable
        override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
      }
      factory(ec)
    }
  }
}
