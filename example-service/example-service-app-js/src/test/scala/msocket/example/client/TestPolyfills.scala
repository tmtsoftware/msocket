package msocket.example.client

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("whatwg-fetch", JSImport.Namespace)
object WhatwgFetch extends js.Object

trait TestPolyfills {
  private val whatwgFetch = WhatwgFetch
}
