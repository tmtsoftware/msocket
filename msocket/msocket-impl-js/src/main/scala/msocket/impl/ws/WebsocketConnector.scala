package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.streaming.Connector
import org.scalajs.dom.raw.WebSocket

class WebsocketConnector[Req: Encoder: ErrorProtocol](uri: String) extends Connector[Req] {
  override def connect[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription = {
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
