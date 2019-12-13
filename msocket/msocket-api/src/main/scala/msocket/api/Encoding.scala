package msocket.api

import java.nio.ByteBuffer

import io.bullet.borer._
import msocket.api.models.ServiceError

import scala.util.Try

abstract class Encoding[E] {
  def encode[T: Encoder](payload: T): E
  def decode[T: Decoder](input: E): T

  def decodeWithServiceError[T: Decoder](input: E): T = Try(decode[T](input)).getOrElse {
    throw decode[ServiceError](input)
  }
  def decodeWithError[T: Decoder, S](input: E)(implicit ep: ErrorProtocol[S]): T = Try(decode[T](input)).getOrElse {
    throw Try(decode[ep.E](input)).getOrElse {
      decode[ServiceError](input)
    }
  }
}

object Encoding {
  case object JsonText extends Encoding[String] {
    def encode[T: Encoder](payload: T): String = Json.encode(payload).toUtf8String
    def decode[T: Decoder](input: String): T   = Json.decode(input.getBytes()).to[T].value
  }

  case object CborByteBuffer extends Encoding[ByteBuffer] {
    override def encode[T: Encoder](payload: T): ByteBuffer = Cbor.encode(payload).toByteBuffer
    override def decode[T: Decoder](input: ByteBuffer): T   = Cbor.decode(input).to[T].value
  }
}
