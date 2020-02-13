package msocket.api

/**
 * The response stream will be materialized to Subscription which will provide a handle for cancellation
 */
trait Subscription {
  def cancel(): Unit
}
