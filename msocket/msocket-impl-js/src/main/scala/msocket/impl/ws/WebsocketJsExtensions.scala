package msocket.impl.ws

import io.bullet.borer._
import msocket.api.Encoding.JsonText
import msocket.api.ErrorProtocol
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.raw.{MessageEvent, WebSocket}

import scala.scalajs.js.typedarray._

object WebsocketJsExtensions {

  implicit class WebsocketJsEncoding(target: Target) {
    def send[T: Encoder](websocket: WebSocket, input: T): Unit = target match {
      case Json => websocket.send(JsonText.encode(input))
      case Cbor => websocket.send(CborArrayBuffer.encode(input))
    }

    def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res = target match {
      case Json => JsonText.decodeWithError(event.data.asInstanceOf[String])
      case Cbor => CborArrayBuffer.decodeWithError(event.data.asInstanceOf[ArrayBuffer])
    }
  }

}
