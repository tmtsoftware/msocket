package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.{CborByteBuffer, JsonText}
import msocket.api.utils.ByteBufferExtensions.RichByteBuffer
import msocket.api.{Encoding, ErrorProtocol}
import org.scalajs.dom.raw.{MessageEvent, WebSocket}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.typedarray._

object WebsocketJsExtensions {

  implicit class WebsocketJsEncoding(encoding: Encoding[_]) {
    def send[T: Encoder](websocket: WebSocket, input: T): Unit = encoding match {
      case CborByteBuffer => websocket.send(CborByteBuffer.encode(input).toByteArray.toTypedArray.buffer)
      case JsonText       => websocket.send(JsonText.encode(input))
      case _              => throw new RuntimeException(s"http-js transport does not support $encoding encoding")
    }

    def response[Res: Decoder, Req: ErrorProtocol](event: MessageEvent)(implicit ec: ExecutionContext): Res = encoding match {
      case CborByteBuffer => CborByteBuffer.decodeWithError(TypedArrayBuffer.wrap(event.data.asInstanceOf[ArrayBuffer]))
      case JsonText       => JsonText.decodeWithError(event.data.asInstanceOf[String])
      case _              => throw new RuntimeException(s"http-js transport does not support $encoding encoding")
    }
  }

}
