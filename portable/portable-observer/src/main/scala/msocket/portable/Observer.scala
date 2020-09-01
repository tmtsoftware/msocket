package msocket.portable

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

trait Observer[-T] {
  def onNext(elm: T): Unit
  def onError(ex: Throwable): Unit
  def onCompleted(): Unit

  def onTry(input: Try[T]): Unit = onTryOption(input.map(Some(_)))

  def onTryOption(input: Try[Option[T]]): Unit =
    input match {
      case Failure(exception)   => onError(exception)
      case Success(Some(value)) => onNext(value)
      case Success(None)        => onCompleted()
    }
}

object Observer {
  def create[T](
      eventHandler: T => Unit = (x: T) => (),
      errorHandler: Throwable => Unit = x => (),
      completionHandler: () => Unit = () => ()
  ): Observer[T] =
    new Observer[T] {
      override def onNext(elm: T): Unit         = eventHandler(elm)
      override def onError(ex: Throwable): Unit = errorHandler(ex)
      override def onCompleted(): Unit          = completionHandler()
    }

  def fromTryOption[T](handler: Try[Option[T]] => Unit): Observer[T] =
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
  def combine[T](observers: () => Seq[Observer[T]]): Observer[T] = fromTryOption(x => observers().foreach(_.onTryOption(x)))
}
