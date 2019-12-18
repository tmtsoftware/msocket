package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

case class FetchEvent(data: String)
object FetchEvent {
  val Heartbeat: FetchEvent                            = FetchEvent("")
  implicit lazy val fetchEventCodec: Codec[FetchEvent] = MapBasedCodecs.deriveCodec
}
