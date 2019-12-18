package msocket.api

import java.nio.ByteBuffer

import io.bullet.borer._
import msocket.api.models.ServiceError

import scala.util.Try

abstract class Encoding[E] {
  def encode[T: Encoder](payload: T): E
  def decode[T: Decoder](input: E): T

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

  class CborBinary[E: Output.ToTypeProvider: Input.Provider] extends Encoding[E] {
    override def encode[T: Encoder](payload: T): E = Cbor.encode(payload).to[E].result
    override def decode[T: Decoder](input: E): T   = Cbor.decode(input).to[T].value
  }

  case object CborByteBuffer extends CborBinary[ByteBuffer]
  case object CborByteArray  extends CborBinary[Array[Byte]]
}
