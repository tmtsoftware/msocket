package mscoket.impl.ws

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.util.ByteString
import io.bullet.borer._
import io.bullet.borer.compat.akka._

sealed abstract class Encoding(val target: Target, val Name: String, val isBinary: Boolean) {
  def strictMessage[T: Encoder](input: T): Message

  def decodeBinary[T: Decoder](input: ByteString): T = target.decode(input).to[T].value
  def decodeText[T: Decoder](input: String): T       = decodeBinary(ByteString(input))

  protected def encodeBinary[T: Encoder](payload: T): ByteString = target.encode(payload).to[ByteString].result
  protected def encodeText[T: Encoder](payload: T): String       = encodeBinary(payload).utf8String
}

object Encoding {
  sealed abstract class BinaryEncoding(target: Target, name: String) extends Encoding(target, name, true) {
    override def strictMessage[T: Encoder](input: T): Message = BinaryMessage.Strict(encodeBinary(input))
  }

  case object JsonText extends Encoding(Json, "json-text", false) {
    override def strictMessage[T: Encoder](input: T): Message = TextMessage.Strict(encodeText(input))
  }

  case object JsonBinary extends BinaryEncoding(Json, "json-binary")
  case object CborBinary extends BinaryEncoding(Cbor, "cbor-binary")

  def fromString(string: String): Encoding = string.toLowerCase match {
    case JsonText.Name   => JsonText
    case JsonBinary.Name => JsonBinary
    case CborBinary.Name => CborBinary
    case encoding        => throw new RuntimeException(s"unsupported encoding: $encoding")
  }
}
