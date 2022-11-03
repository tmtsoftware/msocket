package akka.actor.typed

import org.scalajs.macrotaskexecutor.MacrotaskExecutor

import scala.concurrent.ExecutionContext

class ActorSystem[-T] {
  implicit def executionContext: ExecutionContext = MacrotaskExecutor.Implicits.global
}
