package csw.example.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

/** A domain model used in the API and its codec */
case class Bag(red: Int, green: Int, blue: Int)
object Bag {
  implicit val bagCodec: Codec[Bag] = MapBasedCodecs.deriveCodec
}
