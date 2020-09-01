package msocket.portable

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

trait Observer[-T] {
  def onNext(elm: T): Unit
  def onError(ex: Throwable): Unit
  def onCompleted(): Unit

  def run(input: Try[Option[T]]): Unit =
    input match {
      case Failure(exception)   => onError(exception)
      case Success(Some(value)) => onNext(value)
      case Success(None)        => onCompleted()
    }

  def runTry(input: Try[T]): Unit = run(input.map(Some(_)))
}

object Observer {
  def create[T](
      nextF: T => Unit = (x: T) => (),
      errorF: Throwable => Unit = x => (),
      doneF: () => Unit = () => ()
  ): Observer[T] =
    new Observer[T] {
      override def onNext(elm: T): Unit         = nextF(elm)
      override def onError(ex: Throwable): Unit = errorF(ex)
      override def onCompleted(): Unit          = doneF()
    }

  def from[T](handler: Try[Option[T]] => Unit): Observer[T] =
    new Observer[T] {
      override def onNext(elm: T): Unit         = handler(Success(Some(elm)))
      override def onError(ex: Throwable): Unit = handler(Failure(ex))
      override def onCompleted(): Unit          = handler(Success(None))
    }

  def fromTry[T](handler: Try[T] => Unit): Observer[T] = {
    new Observer[T] {
      override def onNext(elm: T): Unit         = handler(Success(elm))
      override def onError(ex: Throwable): Unit = handler(Failure(ex))
      override def onCompleted(): Unit          = ()
    }
  }

  def fromPromise[T](promise: Promise[T]): Observer[T]           = fromTry(promise.tryComplete)
  def combine[T](observers: () => Seq[Observer[T]]): Observer[T] = from(x => observers().foreach(_.run(x)))
}
