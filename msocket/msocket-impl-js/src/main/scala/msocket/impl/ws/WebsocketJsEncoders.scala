package msocket.impl.ws

import io.bullet.borer._
import msocket.api.Encoding.JsonText
import msocket.api.ErrorProtocol
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw.MessageEvent

import scala.scalajs.js.typedarray._

trait WebsocketJsEncoders[CT <: Target] {
  def send[T: Encoder](websocket: WebSocket, input: T): Unit
  def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res
}

object WebsocketJsEncoders {
  implicit object JsonWebsocketJsEncoders extends WebsocketJsEncoders[Json.type] {
    override def send[T: Encoder](websocket: WebSocket, input: T): Unit = websocket.send(JsonText.encode(input))
    override def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res =
      JsonText.decodeWithError(event.data.asInstanceOf[String])
  }

  implicit object CborWebsocketJsEncoders extends WebsocketJsEncoders[Cbor.type] {
    override def send[T: Encoder](websocket: WebSocket, input: T): Unit = websocket.send(CborArrayBuffer.encode(input))
    override def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res =
      CborArrayBuffer.decodeWithError(event.data.asInstanceOf[ArrayBuffer])
  }
}
