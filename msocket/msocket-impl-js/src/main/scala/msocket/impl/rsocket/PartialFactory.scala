package msocket.impl.rsocket

import typings.std.Partial

object PartialFactory {
  def from[T](x: T): Partial[T] = x.asInstanceOf[Partial[T]]
}
