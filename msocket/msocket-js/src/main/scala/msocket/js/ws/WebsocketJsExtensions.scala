package msocket.js.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType.{Cbor, Json}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.js.CborArrayBuffer
import org.scalajs.dom.{MessageEvent, WebSocket}

import scala.scalajs.js.typedarray._

object WebsocketJsExtensions {

  implicit class WebsocketJsEncoding(contentType: ContentType) {
    def send[T: Encoder](websocket: WebSocket, input: T): Unit =
      contentType match {
        case Json => websocket.send(JsonText.encode(input))
        case Cbor => websocket.send(CborArrayBuffer.encode(input))
      }

    def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res =
      contentType match {
        case Json => JsonText.decodeWithError(event.data.asInstanceOf[String])
        case Cbor => CborArrayBuffer.decodeWithError(event.data.asInstanceOf[ArrayBuffer])
      }
  }

}
