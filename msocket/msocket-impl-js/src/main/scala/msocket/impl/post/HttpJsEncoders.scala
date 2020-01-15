package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.crypto.BufferSource
import org.scalajs.dom.experimental.{BodyInit, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.typedarray.ArrayBuffer

abstract class HttpJsEncoders[En](val mimeType: String) {
  def body[T: Encoder](input: T): BodyInit
  def responseError[Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Throwable]
  def response[Res: Decoder, Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Res]
}

object HttpJsEncoders {
  implicit object JsonHttpJsEncoders extends HttpJsEncoders[String](Encoding.ApplicationJson) {
    override def body[T: Encoder](input: T): BodyInit = JsonText.encode(input)
    override def responseError[Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Throwable] =
      response.text().toFuture.map(x => JsonText.decodeError(x))
    override def response[Res: Decoder, Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Res] =
      response.text().toFuture.map(x => JsonText.decode(x))
  }

  implicit object CborHttpJsEncoders extends HttpJsEncoders[ArrayBuffer](Encoding.ApplicationCbor) {
    override def body[T: Encoder](input: T): BodyInit = CborArrayBuffer.encode(input): BufferSource
    override def responseError[Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Throwable] =
      response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decodeError(x))
    override def response[Res: Decoder, Req: ErrorProtocol](response: Response)(implicit ec: ExecutionContext): Future[Res] =
      response.arrayBuffer().toFuture.map(x => CborArrayBuffer.decode(x))
  }
}
