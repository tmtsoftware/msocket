package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType
import msocket.api.ContentType.{Cbor, Json}
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.crypto.BufferSource
import org.scalajs.dom.experimental.{BodyInit, Response}

import scala.concurrent.{ExecutionContext, Future}

object HttpJsExtensions {

  implicit class HttpJsEncoding(contentType: ContentType) {

    def body[T: Encoder](input: T): BodyInit =
      contentType match {
        case Json => JsonText.encode(input)
        case Cbor => CborArrayBuffer.encode(input): BufferSource
      }

    def response[Res: Decoder, Req](response: Response)(implicit ec: ExecutionContext): Future[Res] =
      contentType match {
        case Json => response.text().toFuture.map(x => JsonText.decode(x))
        case Cbor => response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decode(x))
      }
  }

}
