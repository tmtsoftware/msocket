package portable.akka.extensions

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.github.ghik.silencer.silent

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers

object PortableAkka {

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit @silent actorSystem: ActorSystem[_]): Unit = {
    timers.setTimeout(duration)(body)
  }

  def withEffect[Out, Mat](stream: Source[Out, Mat])(f: Out => Unit): Source[Out, Mat] = {
    stream.subscribe(f)
    stream
  }
}
