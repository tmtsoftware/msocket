package msocket.core.api

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer.{Cbor, Decoder, Json, Target}

sealed abstract class Encoding(val target: Target) {
  def strict(input: Payload[_]): Message
  def streamed(input: Source[Payload[_], NotUsed]): Message

  def decodeBinary[T: Decoder](input: ByteString): T = target.decode(input).to[T].value
  def decodeText[T: Decoder](input: String): T       = decodeBinary(ByteString(input))

  protected def encodeBinary(input: Payload[_]): ByteString = target.encode(input).to[ByteString].result
  protected def encodeText(input: Payload[_]): String       = encodeBinary(input).utf8String
}

object Encoding {
  sealed abstract class BinaryEncoding(target: Target) extends Encoding(target) {
    override def strict(input: Payload[_]): Message                    = BinaryMessage.Strict(encodeBinary(input))
    override def streamed(input: Source[Payload[_], NotUsed]): Message = BinaryMessage.Streamed(input.map(encodeBinary))
  }

  case object JsonText extends Encoding(Json) {
    override def strict(input: Payload[_]): Message                    = TextMessage.Strict(encodeText(input))
    override def streamed(input: Source[Payload[_], NotUsed]): Message = TextMessage.Streamed(input.map(encodeText))
  }

  case object JsonBinary extends BinaryEncoding(Json)
  case object CborBinary extends BinaryEncoding(Cbor)
}
