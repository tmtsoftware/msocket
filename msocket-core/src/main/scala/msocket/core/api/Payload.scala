package msocket.core.api

import java.util.UUID

import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Codec, Decoder, Encoder}

case class Payload[T](response: Response[T], id: UUID)

object Payload {
  implicit lazy val uuidCodec: Codec[UUID] = Codec
    .implicitly[(Long, Long)]
    .bimap[UUID](
      uuid => (uuid.getMostSignificantBits, uuid.getLeastSignificantBits), { case (m, l) => new UUID(m, l) }
    )

//  implicit def payloadEnc2: Encoder[Payload[_]]                     = payloadEnc[Payload[Any]].asInstanceOf[Encoder[Payload[_]]]
  implicit def payloadEnc[T]: Encoder[Payload[T]]                   = deriveEncoder[Payload[T]]
  implicit def payloadDec[T: Decoder: Encoder]: Decoder[Payload[T]] = deriveDecoder[Payload[T]]
}
