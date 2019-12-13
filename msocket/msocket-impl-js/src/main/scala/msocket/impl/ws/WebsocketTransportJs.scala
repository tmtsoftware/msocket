package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import org.scalajs.dom.raw.WebSocket

import scala.concurrent.Future

class WebsocketTransportJs[Req: Encoder: ErrorProtocol](uri: String) extends JsTransport[Req] {

  override def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport"))
  }

  override def requestStream[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription = {
    val webSocket = new WebSocket(uri) {
      onopen = { _ =>
        send(JsonText.encode(req))
        println("connection open")
      }

      onmessage = { event =>
        onMessage(JsonText.decodeWithError(event.data.asInstanceOf[String]))
      }

      onclose = { _ =>
        println("connection closed")
      }
    }

    () => webSocket.close()
  }

}
