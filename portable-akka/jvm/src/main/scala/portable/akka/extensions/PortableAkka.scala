package portable.akka.extensions

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object PortableAkka {

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit actorSystem: ActorSystem[_]): Unit = {
    import actorSystem.executionContext
    akka.pattern.after(duration, actorSystem.scheduler.toClassic) {
      Future.successful(body)
    }
  }

  def withEffect[Out, Mat](stream: Source[Out, Mat])(f: Out => Unit): Source[Out, Mat] = stream.map { x =>
    f(x)
    x
  }
}
