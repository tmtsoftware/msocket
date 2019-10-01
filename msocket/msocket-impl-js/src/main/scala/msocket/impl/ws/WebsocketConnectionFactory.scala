package msocket.impl.ws

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{ConnectedSource, ConnectionFactory}
import org.scalajs.dom.raw.WebSocket

class WebsocketConnectionFactory[Req: Encoder](uri: String) extends ConnectionFactory {
  def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S = {
    val webSocket = new WebSocket(uri) {
      onopen = { _ =>
        send(Json.encode(req).toUtf8String)
        println("connection open")
      }

      onmessage = { event =>
        source.onTextMessage(event.data.asInstanceOf[String])
      }

      onclose = { _ =>
        println("connection closed")
      }
    }
    source.subscription = () => webSocket.close()
    source
  }
}
