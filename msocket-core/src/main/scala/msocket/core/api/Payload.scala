package msocket.core.api

import java.util.UUID

import io.bullet.borer.{Codec, Decoder, Encoder}
import io.bullet.borer.derivation.MapBasedCodecs._

case class Payload[T](message: T, id: UUID)

object Payload {
  implicit lazy val uuidCodec: Codec[UUID] = Codec
    .implicitly[(Long, Long)]
    .bimap[UUID](
      uuid => (uuid.getMostSignificantBits, uuid.getLeastSignificantBits), { case (m, l) => new UUID(m, l) }
    )

  implicit def payloadEnc[T: Encoder]: Encoder[Payload[T]] = deriveEncoder[Payload[T]]
  implicit def payloadDec[T: Decoder]: Decoder[Payload[T]] = deriveDecoder[Payload[T]]
}
