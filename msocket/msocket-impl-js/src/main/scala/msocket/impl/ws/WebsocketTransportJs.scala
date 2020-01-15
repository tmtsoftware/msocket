package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import org.scalajs.dom.raw.WebSocket

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class WebsocketTransportJs[Req: Encoder: ErrorProtocol, En](uri: String)(
    implicit encoders: WebsocketJsEncoders[En],
    ec: ExecutionContext
) extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription = {
    val webSocket = new WebSocket(uri) {

      binaryType = "arraybuffer"

      onopen = { _ =>
        encoders.send(this, request)
        println("connection open")
      }

      onmessage = { event =>
        try onMessage(encoders.response(event))
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
