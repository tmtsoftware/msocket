package msocket.portable

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers

object PortableAkka {

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit @nowarn actorSystem: ActorSystem[_]): Unit = {
    timers.setTimeout(duration)(body)
  }

  def viaObserver[Out, Mat](stream: Source[Out, Mat], observer: Observer[Out])(implicit @nowarn ec: ExecutionContext): Source[Out, Mat] = {
    stream.onMessage(observer)
    stream
  }
}
