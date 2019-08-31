package msocket.api

import java.util.UUID

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

case class WebsocketEvent(id: UUID, data: String)

object WebsocketEvent {
  implicit lazy val uuidCodec: Codec[UUID]                     = Codec.implicitly[String].bimap(_.toString, UUID.fromString)
  implicit lazy val websocketEventCodec: Codec[WebsocketEvent] = MapBasedCodecs.deriveCodec[WebsocketEvent]
}
