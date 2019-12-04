package msocket.impl

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.util.ByteString
import io.bullet.borer._
import io.bullet.borer.compat.akka._
import msocket.api.models.ServiceException

import scala.util.Try

sealed abstract class Encoding[E] {
  def encode[T: Encoder](payload: T): E
  def decode[T: Decoder](input: E): T
  def decodeWithCustomException[T: Decoder](input: E): T = Try(decode[T](input)).getOrElse(throw decode[ServiceException](input))
  def strictMessage[T: Encoder](input: T): Message
}

object Encoding {
  case object CborBinary extends Encoding[ByteString] {
    def encode[T: Encoder](payload: T): ByteString   = Cbor.encode(payload).to[ByteString].result
    def decode[T: Decoder](input: ByteString): T     = Cbor.decode(input).to[T].value
    def strictMessage[T: Encoder](input: T): Message = BinaryMessage.Strict(encode(input))
  }

  case object JsonText extends Encoding[String] {
    def encode[T: Encoder](payload: T): String       = Json.encode(payload).toUtf8String
    def decode[T: Decoder](input: String): T         = Json.decode(ByteString(input)).to[T].value
    def strictMessage[T: Encoder](input: T): Message = TextMessage.Strict(encode(input))
  }
}
