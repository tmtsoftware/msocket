package msocket.core.api

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer.{Cbor, Decoder, Json, Target}

sealed abstract class Encoding(val target: Target) {
  import Payload._
  def strict(input: Payload[_]): Message
  def streamed(input: Source[Payload[_], NotUsed]): Message

  def decode[T: Decoder](input: String): T = target.decode(ByteString(input)).to[T].value
  def isBinary: Boolean

  protected def bytes(input: Payload[_]): ByteString = target.encode(input).to[ByteString].result
  protected def text(input: Payload[_]): String      = bytes(input).utf8String
}

object Encoding {
  sealed abstract class BinaryEncoding(target: Target) extends Encoding(target) {
    override def isBinary: Boolean                                       = true
    override def strict(input: Payload[_]): Message                    = BinaryMessage.Strict(bytes(input))
    override def streamed(input: Source[Payload[_], NotUsed]): Message = BinaryMessage.Streamed(input.map(bytes))
  }

  case object JsonText extends Encoding(Json) {
    override def isBinary: Boolean                                       = false
    override def strict(input: Payload[_]): Message                    = TextMessage.Strict(text(input))
    override def streamed(input: Source[Payload[_], NotUsed]): Message = TextMessage.Streamed(input.map(text))
  }

  case object JsonBinary extends BinaryEncoding(Json)
  case object CborBinary extends BinaryEncoding(Cbor)
}
