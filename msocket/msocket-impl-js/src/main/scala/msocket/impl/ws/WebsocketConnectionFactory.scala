package msocket.impl.ws

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{ConnectedSource, Closeable, ConnectionFactory}
import org.scalajs.dom.raw.WebSocket

class WebsocketConnectionFactory[Req: Encoder](uri: String) extends ConnectionFactory {
  def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S = {
    source.closeable = new WebSocket(uri) with Closeable {
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
    source
  }
}
