package msocket.impl.ws

import io.bullet.borer._
import msocket.api.Encoding.JsonText
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw.MessageEvent

import scala.scalajs.js.typedarray._

trait WebsocketJsEncoders[CT <: Target] {
  type En
  def encoding: Encoding[En]
  def send[T: Encoder](websocket: WebSocket, input: T): Unit

  def encoder[T: Encoder](input: T): En                                    = encoding.encode(input)
  def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res = encoding.decodeWithError(event.data.asInstanceOf[En])
}

object WebsocketJsEncoders {
  abstract class WebsocketJsEncodersFactory[CT <: Target, _En](val encoding: Encoding[_En]) extends WebsocketJsEncoders[CT] {
    override type En = _En
    def send[T: Encoder](websocket: WebSocket, input: T): Unit
  }

  implicit object JsonWebsocketJsEncoders extends WebsocketJsEncodersFactory[Json.type, String](JsonText) {
    override def send[T: Encoder](websocket: WebSocket, input: T): Unit = websocket.send(encoder(input))
  }

  implicit object CborWebsocketJsEncoders extends WebsocketJsEncodersFactory[Cbor.type, ArrayBuffer](CborArrayBuffer) {
    override def send[T: Encoder](websocket: WebSocket, input: T): Unit = websocket.send(encoder(input))
  }
}
