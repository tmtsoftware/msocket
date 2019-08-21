package msocket.simple.server

import io.bullet.borer.Dom.{MapElem, StringElem}
import io.bullet.borer.derivation.MapBasedCodecs
import io.bullet.borer.{Cbor, Codec, Encoder, Json}
import org.scalatest.FunSuite

trait WithMessage {
  def msg: String
}

object WithMessage {
  def codec[T <: WithMessage](defaultCodec: Codec[T]): Codec[T] = Codec(encoder(defaultCodec.encoder), defaultCodec.decoder)

  def encoder[T <: WithMessage](defaultEncoder: Encoder[T]): Encoder[T] = implicitly[Encoder[MapElem]].contramapWithWriter { (w, msg) =>
    val bytes         = w.target.encode(msg)(defaultEncoder).toByteArray
    val mapElem       = w.target.decode(bytes).to[MapElem].value
    val updatedMapElm = mapElem.toMap + (StringElem("msg") -> StringElem(msg.msg))
    mapElem match {
      case _: MapElem.Sized   => MapElem.Sized(updatedMapElm)
      case _: MapElem.Unsized => MapElem.Unsized(updatedMapElm)
    }
  }
}

case class Book(name: String) extends WithMessage {
  override def msg: String = "this is a book"
}

class WithMessageTest extends FunSuite {
  implicit lazy val bookCodec: Codec[Book] = WithMessage.codec(MapBasedCodecs.deriveCodec)

  test("json") {
    val utf8String = Json.encode(Book("abc")).toUtf8String
    println(utf8String)
    val book = Json.decode(utf8String.getBytes()).to[Book].value
    println(book)
  }

  test("cbor") {
    val bytes = Cbor.encode(Book("abc")).toByteArray
    println(bytes)
    val book = Cbor.decode(bytes).to[Book].value
    println(book)
  }
}
