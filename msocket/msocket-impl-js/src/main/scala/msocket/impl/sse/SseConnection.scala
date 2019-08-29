package msocket.impl.sse

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{ConnectedSource, Closeable, Connection}
import typings.eventsource.MessageEvent
import typings.eventsource.eventsourceMod.{EventSourceInitDict, ^ => Sse}

import scala.scalajs.js

class SseConnection[Req: Encoder](uri: String) extends Connection {
  override def start(req: Req, source: ConnectedSource[_, _]): Closeable = {
    new Sse(uri, EventSourceInitDict(js.Dynamic.literal("query" -> Json.encode(req).toUtf8String))) with Closeable {

      override def closeStream(): Unit = close()

      override def onopen(evt: MessageEvent): js.Any = {
        println("connection open")
      }

      override def onmessage(evt: MessageEvent): js.Any = {
        source.onTextMessage(evt.data.asInstanceOf[String])
      }
    }
  }
}
