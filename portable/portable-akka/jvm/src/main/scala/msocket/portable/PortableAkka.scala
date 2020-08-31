package msocket.portable

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedSchedulerOps
import akka.stream.scaladsl.Source

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PortableAkka {

  def setTimeout(duration: FiniteDuration)(body: => Unit)(implicit actorSystem: ActorSystem[_]): Unit = {
    import actorSystem.executionContext
    akka.pattern.after(duration, actorSystem.scheduler.toClassic) {
      Future.successful(body)
    }
  }

  implicit class SourceOps[Out, Mat](private val target: Source[Out, Mat]) extends AnyVal {
    def viaObserver(observer: Observer[Out])(implicit ec: ExecutionContext): Source[Out, Mat] = {
      target
        .map { x =>
          observer.onNext(x)
          x
        }
        .watchTermination() { (mat, doneF) =>
          doneF.onComplete {
            case Failure(exception) => observer.onError(exception);
            case Success(_)         => observer.onCompleted()
          }
          mat
        }
    }
  }

}
