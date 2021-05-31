package msocket.js.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.js.JsTransport
import msocket.js.ws.WebsocketJsExtensions.WebsocketJsEncoding
import msocket.portable.Observer
import org.scalajs.dom.raw.WebSocket

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class WebsocketTransportJs[Req: Encoder: ErrorProtocol](uri: String, contentType: ContentType)(implicit ec: ExecutionContext)
    extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {
    val webSocket = new WebSocket(uri) {

      binaryType = "arraybuffer"

      onopen = { _ =>
        contentType.send(this, request)
        println("websocket connection open")
      }

      onmessage = { event =>
        try observer.onNext(contentType.response(event))
        catch {
          case NonFatal(ex) =>
            observer.onError(ex)
            close()
        }
      }

      onclose = { x =>
        observer.onCompleted()
      }

      onerror = { e =>
        observer.onError(new RuntimeException(s"websocket connection error=$e"))
        close()
      }
    }

    () => {
      webSocket.close()
    }
  }

}
