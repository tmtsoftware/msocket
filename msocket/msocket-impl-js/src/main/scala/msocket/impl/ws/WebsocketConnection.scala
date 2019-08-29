package msocket.impl.ws

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{ConnectedSource, Closeable, Connection}
import org.scalajs.dom.raw.WebSocket

class WebsocketConnection[Req: Encoder](uri: String) extends Connection {
  override def start(req: Req, source: ConnectedSource[_, _]): Closeable = {
    new WebSocket(uri) with Closeable {
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

      override def closeStream(): Unit = close()
    }
  }
}
