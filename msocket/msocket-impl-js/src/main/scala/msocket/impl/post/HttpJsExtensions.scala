package msocket.impl.post

import io.bullet.borer._
import msocket.api.Encoding.JsonText
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.crypto.BufferSource
import org.scalajs.dom.experimental.{BodyInit, Response}

import scala.concurrent.{ExecutionContext, Future}

object HttpJsExtensions {

  implicit class HttpJsEncoding(encoding: Target) {
    def body[T: Encoder](input: T): BodyInit = encoding match {
      case Json => JsonText.encode(input)
      case Cbor => CborArrayBuffer.encode(input): BufferSource
    }

    def responseError[Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Throwable] = encoding match {
      case Json => response.text().toFuture.map(x => JsonText.decodeError(x))
      case Cbor => response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decodeError(x))
    }

    def response[Res: Decoder, Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Res] = encoding match {
      case Json => response.text().toFuture.map(x => JsonText.decode(x))
      case Cbor => response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decode(x))
    }

    def mimeType: String = encoding match {
      case Json => Encoding.ApplicationJson
      case Cbor => Encoding.ApplicationCbor
    }
  }

}
