package msocket.simple.server

import io.bullet.borer.Dom.{MapElem, StringElem}
import io.bullet.borer.derivation.MapBasedCodecs
import io.bullet.borer.{Cbor, Codec, Decoder, Encoder, Json}
import org.scalatest.FunSuite

trait ErrMsg {
  def msg: String
}

object ErrMsg {
  def codec[T <: ErrMsg](codec: Codec[T]): Codec[T] = Codec(encoder(codec.encoder), decoder(codec.decoder))

  def encoder[T <: ErrMsg](encoder: Encoder[T]): Encoder[T] = implicitly[Encoder[MapElem]].contramapWithWriter { (w, msg) =>
    val bytes         = w.target.encode(msg)(encoder).toByteArray
    val mapElem       = w.target.decode(bytes).to[MapElem].value
    val updatedMapElm = mapElem.toMap + (StringElem("msg") -> StringElem(msg.msg))
    mapElem match {
      case _: MapElem.Sized   => MapElem.Sized(updatedMapElm)
      case _: MapElem.Unsized => MapElem.Unsized(updatedMapElm)
    }
  }

  def decoder[T <: ErrMsg](decoder: Decoder[T]): Decoder[T] = implicitly[Decoder[MapElem]].mapWithReader { (r, mapElm) =>
    val bytes = r.target.encode(mapElm).toByteArray
    r.target.decode(bytes).to[T](decoder).value
  }
}

case class Book(name: String) extends ErrMsg {
  override def msg: String = "this is a book"
}

class ErrMsgTest extends FunSuite {
  implicit lazy val bookCodec: Codec[Book] = ErrMsg.codec(MapBasedCodecs.deriveCodec)

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
