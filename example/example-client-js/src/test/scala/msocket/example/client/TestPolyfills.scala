package msocket.example.client

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("whatwg-fetch", JSImport.Namespace)
object WhatwgFetch extends js.Object

trait TestPolyfills {
  @nowarn private val whatwgFetch = WhatwgFetch
}
