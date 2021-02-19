package msocket.js.rsocket

import tmttyped.std.Partial

object PartialOf {
  def apply[T](x: T): Partial[T] = x.asInstanceOf[Partial[T]]
}
