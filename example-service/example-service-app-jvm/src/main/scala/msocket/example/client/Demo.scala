package msocket.example.client

import io.bullet.borer.derivation.CompactMapBasedCodecs
import io.bullet.borer.{Codec, Json}

object Demo {

  sealed trait Base
  case class A(a: Int)    extends Base
  case class B(b: String) extends Base
  implicit lazy val codec: Codec[Base] = CompactMapBasedCodecs.deriveAllCodecs

  def encode(x: Base): String = Json.encode(x).toUtf8String

  def main(args: Array[String]): Unit = {
    println(encode(B("abc")))
  }

}
