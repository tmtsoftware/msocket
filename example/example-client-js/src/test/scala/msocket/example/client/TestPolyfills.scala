package msocket.example.client

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object TestPolyfills {
  @JSImport("global-jsdom", JSImport.Default)
  @js.native
  def GlobalJsdom(): js.Function0[Unit] = js.native

  @JSImport("fast-text-encoding", JSImport.Namespace)
  @js.native
  object FastTextEncoding extends js.Object

  @JSImport("cross-fetch/polyfill", JSImport.Namespace)
  @js.native
  object CrossFetch extends js.Object

  @js.native
  @JSImport("@stardazed/streams-polyfill", JSImport.Namespace)
  object StardazedStreamsPolyfill extends js.Object
}
