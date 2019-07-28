package msocket.core.api

import java.util.UUID

import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Codec, Decoder, Encoder}

case class Envelope[T](payload: Payload[T], id: UUID)

object Envelope {
  implicit lazy val uuidCodec: Codec[UUID] = Codec
    .implicitly[(Long, Long)]
    .bimap[UUID](
      uuid => (uuid.getMostSignificantBits, uuid.getLeastSignificantBits), { case (m, l) => new UUID(m, l) }
    )

  implicit def envelopeEnc[T]: Encoder[Envelope[T]]                   = deriveEncoder[Envelope[T]]
  implicit def envelopeDec[T: Decoder: Encoder]: Decoder[Envelope[T]] = deriveDecoder[Envelope[T]]
}
