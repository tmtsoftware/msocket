package msocket.impl.post

import scala.scalajs.js

@js.native
trait FetchEventJs extends js.Object {
  def data: String                  = js.native
  def errorType: js.UndefOr[String] = js.native
}

object FetchEventJs {
  def apply(obj: js.Object): FetchEventJs = obj.asInstanceOf[FetchEventJs]
}
