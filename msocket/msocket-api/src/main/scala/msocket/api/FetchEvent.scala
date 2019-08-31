package msocket.api

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

case class FetchEvent(data: String)
object FetchEvent {
  val Heartbeat: FetchEvent                            = FetchEvent("")
  implicit lazy val fetchEventCodec: Codec[FetchEvent] = deriveCodec[FetchEvent]
}
