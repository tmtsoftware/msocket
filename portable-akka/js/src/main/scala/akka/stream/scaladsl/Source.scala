package akka.stream.scaladsl

import akka.NotUsed

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait Source[+Out, +Mat] {
  val materializedValue: Mat
  def onMessage(handler: Out => Unit): Unit
  def onError(handler: Throwable => Unit): Unit
}

object Source {
  def future[T](futureElement: Future[T]): Source[T, NotUsed] =
    new Source[T, NotUsed] {
      override val materializedValue: NotUsed          = NotUsed
      override def onMessage(handler: T => Unit): Unit = futureElement.foreach(handler)

      override def onError(handler: Throwable => Unit): Unit = {
        futureElement.onComplete {
          case Failure(exception) => handler(exception)
          case Success(_)         =>
        }
      }
    }
}
