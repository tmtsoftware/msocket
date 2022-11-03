package akka.stream.scaladsl

import akka.NotUsed
import msocket.portable.Observer

import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import scala.concurrent.Future

trait Source[+Out, +Mat] {
  val subscription: Mat
  def onMessage(observer: Observer[Out]): Unit

  def onNext(handler: Out => Unit): Unit        = onMessage(Observer.create(handler))
  def onError(handler: Throwable => Unit): Unit = onMessage(Observer.create(errorHandler = handler))
  def onCompleted(handler: () => Unit): Unit    = onMessage(Observer.create(completionHandler = handler))
}

object Source {
  def future[T](futureElement: Future[T]): Source[T, NotUsed] =
    new Source[T, NotUsed] {
      override val subscription: NotUsed                  = NotUsed
      override def onMessage(observer: Observer[T]): Unit = futureElement.onComplete(observer.onTry)
    }
}
