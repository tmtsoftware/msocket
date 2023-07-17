package org.apache.pekko.actor.typed

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ActorSystem[-T] {
  implicit def executionContext: ExecutionContextExecutor = ExecutionContext.global
}
