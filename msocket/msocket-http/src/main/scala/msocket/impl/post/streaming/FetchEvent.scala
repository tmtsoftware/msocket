package msocket.impl.post.streaming

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

case class FetchEvent(data: String, errorType: Option[String] = None)
object FetchEvent {
  val Heartbeat: FetchEvent                            = FetchEvent("")
  implicit lazy val fetchEventCodec: Codec[FetchEvent] = MapBasedCodecs.deriveCodec
}
