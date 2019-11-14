package portable.akka.extensions

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object PortableAkka {

  implicit class SourceWithSubscribe[Out, Mat](x: Source[Out, Mat]) {
    def subscribe(f: Out => Unit)(implicit actorSystem: ActorSystem[_]): Mat = {
      x.to(Sink.foreach(f)).run()
    }
  }

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit actorSystem: ActorSystem[_]): Unit = {
    import actorSystem.executionContext
    akka.pattern.after(duration, actorSystem.scheduler.toClassic) {
      Future(body)
    }
  }

}
