package msocket.impl.rsocket

import java.nio.ByteBuffer

import io.bullet.borer.Decoder
import msocket.api.ContentEncoding.{CborBinary, CborByteArray}
import msocket.api.ErrorProtocol
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer

case object CborByteBuffer extends CborBinary[ByteBuffer] {
  override def decodeWithError[T: Decoder, S](input: ByteBuffer)(implicit ep: ErrorProtocol[S]): T =
    CborByteArray.decodeWithError(input.toByteArray)

  override def decodeError[S](input: ByteBuffer)(implicit ep: ErrorProtocol[S]): Throwable = CborByteArray.decodeError(input.toByteArray)
}
