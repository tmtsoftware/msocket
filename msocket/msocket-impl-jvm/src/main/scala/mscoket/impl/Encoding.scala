package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer._
import msocket.api.Payload

sealed abstract class Encoding(val target: Target, val Name: String, val isBinary: Boolean) {
  def strictMessage(input: Payload[_]): Message
  def strictMessageStream(input: Source[Payload[_], NotUsed]): Source[Message, NotUsed] = input.map(strictMessage)

  def decodeBinary[T: Decoder: Encoder](input: ByteString): Payload[T] = target.decode(input).to[Payload[T]].value
  def decodeText[T: Decoder: Encoder](input: String): Payload[T]       = decodeBinary(ByteString(input))

  protected def encodeBinary(payload: Payload[_]): ByteString = target.encode(payload).to[ByteString].result
  protected def encodeText(payload: Payload[_]): String       = encodeBinary(payload).utf8String
}

object Encoding {
  sealed abstract class BinaryEncoding(target: Target, name: String) extends Encoding(target, name, true) {
    override def strictMessage(input: Payload[_]): Message = BinaryMessage.Strict(encodeBinary(input))
  }

  case object JsonText extends Encoding(Json, "json-text", false) {
    override def strictMessage(input: Payload[_]): Message = TextMessage.Strict(encodeText(input))
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
