package msocket.simple.server

import io.bullet.borer.{Codec, Json}
import org.scalatest.FunSuite
//import io.bullet.borer.derivation.MapBasedCodecs._
//import io.bullet.borer.derivation.ArrayBasedCodecs.deriveUnaryEncoder
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveUnaryCodec

case class Box(x: Int)

object Box {
//  implicit lazy val boxCodec: Codec[Box] = deriveCodec[Box]
  implicit lazy val boxCodec: Codec[Box] = deriveUnaryCodec[Box]
//  implicit lazy val boxEnc: Encoder[Box] = deriveUnaryEncoder[Box]
//  implicit lazy val boxDec: Decoder[Box] = deriveDecoder[Box]
}
class CodecTest extends FunSuite {

  test("demo") {
    val box        = Box(100)
    val jsonString = Json.encode(box).toUtf8String
    println(jsonString)
    val actual = Json.decode(jsonString.getBytes()).to[Box].value
    println(actual)
  }

}
