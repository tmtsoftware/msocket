package msocket.core.api

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.bullet.borer.{Cbor, Encoder, Json, Target}
import io.bullet.borer.compat.akka._

sealed abstract class Encoding(val target: Target) {
  def strict[T: Encoder](input: T): Message
  def streamed[T: Encoder](input: Source[T, NotUsed]): Message

  protected def bytes[T: Encoder](input: T): ByteString = target.encode(input).to[ByteString].result
  protected def text[T: Encoder](input: T): String      = bytes(input).utf8String
}

object Encoding {
  sealed abstract class BinaryEncoding(target: Target) extends Encoding(target) {
    override def strict[T: Encoder](input: T): Message                    = BinaryMessage.Strict(bytes(input))
    override def streamed[T: Encoder](input: Source[T, NotUsed]): Message = BinaryMessage.Streamed(input.map(bytes(_)))
  }

  case object JsonText extends Encoding(Json) {
    override def strict[T: Encoder](input: T): Message                    = TextMessage.Strict(text(input))
    override def streamed[T: Encoder](input: Source[T, NotUsed]): Message = TextMessage.Streamed(input.map(text(_)))
  }

  case object JsonBinary extends BinaryEncoding(Json)
  case object CborBinary extends BinaryEncoding(Cbor)
}
