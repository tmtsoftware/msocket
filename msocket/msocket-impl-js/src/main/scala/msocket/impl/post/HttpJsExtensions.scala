package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.crypto.BufferSource
import org.scalajs.dom.experimental.{BodyInit, Response}

import scala.concurrent.{ExecutionContext, Future}

object HttpJsExtensions {

  implicit class HttpJsEncoding(encoding: Encoding[_]) {
    def body[T: Encoder](input: T): BodyInit = encoding match {
      case CborArrayBuffer => CborArrayBuffer.encode(input): BufferSource
      case JsonText        => JsonText.encode(input)
      case _               => throw new RuntimeException(s"http-js transport does not support $encoding encoding")
    }

    def responseError[Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Throwable] = encoding match {
      case CborArrayBuffer => response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decodeError(x))
      case JsonText        => response.text().toFuture.map(x => JsonText.decodeError(x))
      case _               => Future.successful(new RuntimeException(s"http-js transport does not support $encoding encoding"))
    }

    def response[Res: Decoder, Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Res] = encoding match {
      case CborArrayBuffer => response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decode(x))
      case JsonText        => response.text().toFuture.map(x => JsonText.decode(x))
      case _               => Future.failed(new RuntimeException(s"http-js transport does not support $encoding encoding"))
    }
  }

}
