package msocket.js.post

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType
import msocket.api.ContentType.{Cbor, Json}
import msocket.js.CborArrayBuffer
import org.scalajs.dom.experimental.{BodyInit, Response}
import org.scalajs.dom.raw.Blob

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8Array}

object HttpJsExtensions {

  implicit class HttpJsEncoding(contentType: ContentType) {

    def body[T: Encoder](input: T): BodyInit =
      contentType match {
        case Json => JsonText.encode(input)
        case Cbor =>
          val buffer: ArrayBuffer = CborArrayBuffer.encode(input)
          new Blob(js.Array(new Uint8Array(buffer)))
      }

    def response[Res: Decoder, Req](response: Response)(implicit ec: ExecutionContext): Future[Res] =
      contentType match {
        case Json => response.text().toFuture.map(x => JsonText.decode(x))
        case Cbor => response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decode(x))
      }
  }

}
