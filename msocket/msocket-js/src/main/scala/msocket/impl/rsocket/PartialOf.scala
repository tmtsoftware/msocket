package msocket.impl.rsocket

import typings.std.Partial

object PartialOf {
  def apply[T](x: T): Partial[T] = x.asInstanceOf[Partial[T]]
}
