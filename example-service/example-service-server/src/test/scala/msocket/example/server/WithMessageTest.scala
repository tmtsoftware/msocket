package msocket.example.server

import io.bullet.borer.Dom.{Element, StringElem}
import io.bullet.borer.derivation.CompactMapBasedCodecs
import io.bullet.borer.{Cbor, Codec, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite

trait WithMessage {
  def msg: String
}

object WithMessage {
  def codec[T <: WithMessage](defaultCodec: Codec[T]): Codec[T] = Codec(encoder(defaultCodec.encoder), defaultCodec.decoder)

  def encoder[T <: WithMessage](defaultEncoder: Encoder[T]): Encoder[T] = implicitly[Encoder[Map[Element, Element]]].contramapWithWriter {
    (w, msg) =>
      val bytes   = w.target.encode(msg)(defaultEncoder).toByteArray
      val mapElem = w.target.decode(bytes).to[Map[Element, Element]].value
      mapElem + (StringElem("msg") -> StringElem(msg.msg))
  }
}

case class Book(name: String) extends WithMessage {
  override def msg: String = "this is a book"
}

class WithMessageTest extends AnyFunSuite {
  implicit lazy val bookCodec: Codec[Book] = WithMessage.codec(CompactMapBasedCodecs.deriveCodec)

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
