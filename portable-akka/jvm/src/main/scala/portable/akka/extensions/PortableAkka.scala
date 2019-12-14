package portable.akka.extensions

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object PortableAkka {

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit actorSystem: ActorSystem[_]): Unit = {
    import actorSystem.executionContext
    akka.pattern.after(duration, actorSystem.scheduler.toClassic) {
      Future.successful(body)
    }
  }

}
