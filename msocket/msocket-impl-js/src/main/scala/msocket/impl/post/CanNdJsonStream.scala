package msocket.impl.post

import org.scalajs.dom.experimental.ReadableStream

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.typedarray.Uint8Array

@JSImport("can-ndjson-stream", JSImport.Namespace)
@js.native
class CanNdJsonStream extends ReadableStream[js.Object] {
  def this(response: ReadableStream[Uint8Array]) = this()
}
