package portable.akka.extensions

import akka.actor.typed.ActorSystem
import com.github.ghik.silencer.silent

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers

object PortableAkka {

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit @silent actorSystem: ActorSystem[_]): Unit = {
    timers.setTimeout(duration)(body)
  }

}
