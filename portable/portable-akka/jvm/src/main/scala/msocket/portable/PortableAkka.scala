package msocket.portable

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedSchedulerOps
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

object PortableAkka {

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit actorSystem: ActorSystem[_]): Unit = {
    import actorSystem.executionContext
    akka.pattern.after(duration, actorSystem.scheduler.toClassic) {
      Future.successful(body)
    }
  }

  def onNext[Out, Mat](stream: Source[Out, Mat])(messageHandler: Out => Unit): Source[Out, Mat] =
    stream.map { x =>
      messageHandler(x)
      x
    }

  def onError[Out, Mat](stream: Source[Out, Mat])(errorHandler: Throwable => Unit): Source[Out, Mat] =
    stream.mapError {
      case NonFatal(ex) =>
        errorHandler(ex);
        ex
    }

  def withEffects[Out, Mat](stream: Source[Out, Mat])(messageHandler: Out => Unit, errorHandler: Throwable => Unit): Source[Out, Mat] =
    onError(onNext(stream)(messageHandler))(errorHandler)
}
