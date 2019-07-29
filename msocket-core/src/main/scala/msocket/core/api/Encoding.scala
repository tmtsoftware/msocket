package msocket.core.api

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer.{Cbor, Decoder, Encoder, Json, Target}

sealed abstract class Encoding(val target: Target) {
  def strict(input: Payload[_]): Message
  def streamed(input: Source[Payload[_], NotUsed]): Message

  def decodeBinary[T: Decoder: Encoder](input: ByteString): Payload[T] = target.decode(input).to[Payload[T]].value
  def decodeText[T: Decoder: Encoder](input: String): Payload[T]       = decodeBinary(ByteString(input))

  protected def encodeBinary(payload: Payload[_]): ByteString = target.encode(payload).to[ByteString].result
  protected def encodeText(payload: Payload[_]): String       = encodeBinary(payload).utf8String
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
