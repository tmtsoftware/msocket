package portable.akka.extensions

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.github.ghik.silencer.silent

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers

object PortableAkka {

  implicit class SourceWithSubscribe[Out, Mat](x: Source[Out, Mat]) {
    def subscribe(f: Out => Unit)(implicit @silent actorSystem: ActorSystem[_]): Mat = {
      x.foreach(f)
      x.materializedValue
    }
  }

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit @silent actorSystem: ActorSystem[_]): Unit = {
    timers.setTimeout(duration)(body)
  }

}
