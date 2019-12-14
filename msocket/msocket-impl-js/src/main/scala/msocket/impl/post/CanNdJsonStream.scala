package msocket.impl.post

import typings.std.{ReadableStream, Uint8Array}

import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSImport("can-ndjson-stream", JSImport.Namespace)
@js.native
class CanNdJsonStream extends ReadableStream[js.Object] {
  def this(response: ReadableStream[Uint8Array]) = this()
}
