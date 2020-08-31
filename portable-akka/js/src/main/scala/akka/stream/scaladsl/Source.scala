package akka.stream.scaladsl

import akka.NotUsed

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait Source[+Out, +Mat] {
  val subscription: Mat
  def onMessage(handler: Try[Option[Out]] => Unit): Unit

  def onNext(handler: Out => Unit): Unit =
    onMessage {
      case Success(Some(value)) => handler(value)
      case _                    =>
    }

  def onError(handler: Throwable => Unit): Unit =
    onMessage {
      case Failure(ex) => handler(ex)
      case _           =>
    }

  def onCompleted(handler: () => Unit): Unit =
    onMessage {
      case Success(None) => handler()
      case _             =>
    }
}

object Source {
  def future[T](futureElement: Future[T]): Source[T, NotUsed] =
    new Source[T, NotUsed] {
      override val subscription: NotUsed                            = NotUsed
      override def onMessage(handler: Try[Option[T]] => Unit): Unit = futureElement.map(Some(_)).onComplete(handler)
    }
}
