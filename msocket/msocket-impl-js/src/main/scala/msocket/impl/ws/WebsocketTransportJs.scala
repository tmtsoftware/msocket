package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.CborBinary
import msocket.api.{Encoding, ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import msocket.impl.ws.WebsocketJsExtensions._
import org.scalajs.dom.raw.WebSocket

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class WebsocketTransportJs[Req: Encoder: ErrorProtocol](uri: String, encoding: Encoding[_])(implicit ec: ExecutionContext)
    extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription = {
    val webSocket = new WebSocket(uri) {

      if (encoding.isInstanceOf[CborBinary[_]]) {
        binaryType = "arraybuffer"
      }

      onopen = { _ =>
        encoding.send(this, request)
        println("connection open")
      }

      onmessage = { event =>
        try onMessage(encoding.response(event))
        catch {
          case NonFatal(ex) => onError(ex); close()
        }
      }

      onclose = { _ =>
        println("connection closed")
      }

      onerror = { e =>
        println(e)
      }
    }

    () => webSocket.close()
  }

}
