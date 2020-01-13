package msocket.api

import java.nio.ByteBuffer

import io.bullet.borer._
import msocket.api.models.ServiceError
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer

import scala.util.Try

abstract class Encoding[E](val mimeType: String) {
  def encode[T: Encoder](payload: T): E
  def decode[T: Decoder](input: E): T

  def decodeWithError[T: Decoder, S](input: E)(implicit ep: ErrorProtocol[S]): T = Try(decode[T](input)).getOrElse(throw decodeError(input))
  def decodeError[S](input: E)(implicit ep: ErrorProtocol[S]): Throwable         = Try(decode[ep.E](input)).getOrElse(decode[ServiceError](input))
}

object Encoding {
  val ApplicationJson = "application/json"
  val ApplicationCbor = "application/cbor"

  case object JsonText extends Encoding[String](ApplicationJson) {
    def encode[T: Encoder](payload: T): String = Json.encode(payload).toUtf8String
    def decode[T: Decoder](input: String): T   = Json.decode(input.getBytes()).to[T].value
  }

  class CborBinary[E: Output.ToTypeProvider: Input.Provider] extends Encoding[E](ApplicationCbor) {
    override def encode[T: Encoder](payload: T): E = Cbor.encode(payload).to[E].result
    override def decode[T: Decoder](input: E): T   = Cbor.decode(input).to[T].value
  }

  case object CborByteBuffer extends CborBinary[ByteBuffer] {
    private case object CborByteArray extends CborBinary[Array[Byte]]

    override def decodeWithError[T: Decoder, S](input: ByteBuffer)(implicit ep: ErrorProtocol[S]): T =
      CborByteArray.decodeWithError(input.toByteArray)

    override def decodeError[S](input: ByteBuffer)(implicit ep: ErrorProtocol[S]): Throwable = CborByteArray.decodeError(input.toByteArray)
  }

  def fromMimeType(mimeType: String): Encoding[_] = mimeType match {
    case ApplicationJson => JsonText
    case ApplicationCbor => CborByteBuffer
    case _               => throw new RuntimeException(s"unsupported mimeType: $mimeType ")
  }
}
