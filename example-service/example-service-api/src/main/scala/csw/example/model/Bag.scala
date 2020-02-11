package csw.example.model

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs

case class Bag(red: Int , green: Int, blue: Int)
object Bag {
  implicit val bagCodec: Codec[Bag] = MapBasedCodecs.deriveCodec
}
