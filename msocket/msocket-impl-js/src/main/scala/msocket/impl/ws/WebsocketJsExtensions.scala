package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.CborArrayBuffer
import org.scalajs.dom.raw.{MessageEvent, WebSocket}

import scala.scalajs.js.typedarray._

object WebsocketJsExtensions {

  implicit class WebsocketJsEncoding(encoding: Encoding[_]) {
    def send[T: Encoder](websocket: WebSocket, input: T): Unit = encoding match {
      case CborArrayBuffer => websocket.send(CborArrayBuffer.encode(input))
      case JsonText        => websocket.send(JsonText.encode(input))
      case _               => throw new RuntimeException(s"http-js transport does not support $encoding encoding")
    }

    def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent): Res = encoding match {
      case CborArrayBuffer => CborArrayBuffer.decodeWithError(event.data.asInstanceOf[ArrayBuffer])
      case JsonText        => JsonText.decodeWithError(event.data.asInstanceOf[String])
      case _               => throw new RuntimeException(s"http-js transport does not support $encoding encoding")
    }
  }

}
