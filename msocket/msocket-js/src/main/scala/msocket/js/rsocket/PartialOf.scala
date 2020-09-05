package msocket.js.rsocket

import typings.std.Partial

object PartialOf {
  def apply[T](x: T): Partial[T] = x.asInstanceOf[Partial[T]]
}
