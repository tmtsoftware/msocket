package msocket.api

import java.nio.ByteBuffer

import io.bullet.borer._
import msocket.api.models.{ErrorType, ServiceError}
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer

import scala.util.Try

abstract class ContentEncoding[E](val contentType: ContentType) {
  def encode[T: Encoder](payload: T): E
  def decode[T: Decoder](input: E): T

  def decodeFull[Res: Decoder, Req](input: E, errorType: Option[ErrorType])(implicit ep: ErrorProtocol[Req]): Res = {
    errorType match {
      case None                         => decode(input)
      case Some(ErrorType.DomainError)  => throw decode[ep.E](input)
      case Some(ErrorType.GenericError) => throw decode[ServiceError](input)
    }
  }
  def decodeWithError[T: Decoder, S](input: E)(implicit ep: ErrorProtocol[S]): T = Try(decode[T](input)).getOrElse(throw decodeError(input))
  def decodeError[S](input: E)(implicit ep: ErrorProtocol[S]): Throwable         = Try(decode[ep.E](input)).getOrElse(decode[ServiceError](input))
}

object ContentEncoding {
  case object JsonText extends ContentEncoding[String](ContentType.Json) {
    def encode[T: Encoder](payload: T): String = Json.encode(payload).toUtf8String
    def decode[T: Decoder](input: String): T   = Json.decode(input.getBytes()).to[T].value
  }

  class CborBinary[E: Output.ToTypeProvider: Input.Provider] extends ContentEncoding[E](ContentType.Cbor) {
    override def encode[T: Encoder](payload: T): E = Cbor.encode(payload).to[E].result
    override def decode[T: Decoder](input: E): T   = Cbor.decode(input).to[T].value
  }

  case object CborByteArray extends CborBinary[Array[Byte]]

  case object CborByteBuffer extends CborBinary[ByteBuffer] {
    override def decodeWithError[T: Decoder, S](input: ByteBuffer)(implicit ep: ErrorProtocol[S]): T =
      CborByteArray.decodeWithError(input.toByteArray)

    override def decodeError[S](input: ByteBuffer)(implicit ep: ErrorProtocol[S]): Throwable = CborByteArray.decodeError(input.toByteArray)
  }
}
