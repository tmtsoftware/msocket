package msocket.api

import akka.Done

import scala.concurrent.{Future, Promise}

/**
 * The response stream will be materialized to Subscription which will provide a handle for cancellation
 */
trait Subscription {
  protected def onCancellation(): Unit

  private lazy val promise: Promise[Done] = Promise()

  def cancel(): Unit = {
    onCancellation()
    promise.success(Done)
  }

  def cancellation: Future[Done] = promise.future
}
