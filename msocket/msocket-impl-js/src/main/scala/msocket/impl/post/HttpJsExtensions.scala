package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.{CborByteBuffer, JsonText}
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer
import msocket.api.{Encoding, ErrorProtocol}
import org.scalajs.dom.crypto.BufferSource
import org.scalajs.dom.experimental.{BodyInit, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.typedarray._

object HttpJsExtensions {

  implicit class HttpJsEncoding(encoding: Encoding[_]) {
    def body[T: Encoder](input: T): BodyInit = encoding match {
      case CborByteBuffer => CborByteBuffer.encode(input).toByteArray.toTypedArray: BufferSource
      case JsonText       => JsonText.encode(input)
      case _              => throw new RuntimeException(s"http-js transport does not support $encoding encoding")
    }

    def responseError[Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Throwable] =
      encoding match {
        case CborByteBuffer => response.arrayBuffer().toFuture.map(x => CborByteBuffer.decodeError(TypedArrayBuffer.wrap(x)))
        case JsonText       => response.text().toFuture.map(x => JsonText.decodeError(x))
        case _              => Future.successful(new RuntimeException(s"http-js transport does not support $encoding encoding"))
      }

    def response[Res: Decoder, Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Res] =
      encoding match {
        case CborByteBuffer => response.arrayBuffer().toFuture.map(x => CborByteBuffer.decodeWithError(TypedArrayBuffer.wrap(x)))
        case JsonText       => response.text().toFuture.map(x => JsonText.decodeWithError(x))
        case _              => Future.failed(new RuntimeException(s"http-js transport does not support $encoding encoding"))
      }
  }

}
