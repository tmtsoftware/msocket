package akka.stream.scaladsl

import akka.NotUsed
import msocket.portable.Observer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Source[+Out, +Mat] {
  val subscription: Mat
  def onMessage(observer: Observer[Out]): Unit

  def onNext(handler: Out => Unit): Unit        = onMessage(Observer.create(handler))
  def onError(handler: Throwable => Unit): Unit = onMessage(Observer.create(errorF = handler))
  def onCompleted(handler: () => Unit): Unit    = onMessage(Observer.create(doneF = handler))
}

object Source {
  def future[T](futureElement: Future[T]): Source[T, NotUsed] =
    new Source[T, NotUsed] {
      override val subscription: NotUsed = NotUsed
      override def onMessage(observer: Observer[T]): Unit = {
        futureElement.onComplete(x => observer.run(x.map(Some(_))))
      }
    }
}
