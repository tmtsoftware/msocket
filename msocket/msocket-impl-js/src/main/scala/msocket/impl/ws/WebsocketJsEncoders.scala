package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw.MessageEvent

import scala.scalajs.js.typedarray._

abstract class WebsocketJsEncoders[En](encoding: Encoding[En]) {
  def send[T: Encoder](websocket: WebSocket, input: T): Unit

  def encoder[T: Encoder](input: T): En                                    = encoding.encode(input)
  def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res = encoding.decodeWithError(event.data.asInstanceOf[En])
}

object WebsocketJsEncoders {
  implicit object JsonWebsocketJsEncoders extends WebsocketJsEncoders[String](JsonText) {
    override def send[T: Encoder](websocket: WebSocket, input: T): Unit = websocket.send(encoder(input))
  }

  implicit object CborWebsocketJsEncoders extends WebsocketJsEncoders[ArrayBuffer](CborArrayBuffer) {
    override def send[T: Encoder](websocket: WebSocket, input: T): Unit = websocket.send(encoder(input))
  }
}
