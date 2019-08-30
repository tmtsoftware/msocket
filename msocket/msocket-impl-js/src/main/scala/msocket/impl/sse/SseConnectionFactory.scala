package msocket.impl.sse

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{ConnectedSource, Closeable, ConnectionFactory}
import typings.eventsource.MessageEvent
import typings.eventsource.eventsourceMod.{EventSourceInitDict, ^ => Sse}

import scala.scalajs.js

class SseConnectionFactory[Req: Encoder](uri: String) extends ConnectionFactory {
  override def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S = {
    source.closeable = new Sse(uri, EventSourceInitDict(queryHeader(req))) with Closeable {

      override def closeStream(): Unit = close()

      override def onopen(evt: MessageEvent): js.Any = {
        println("connection open")
      }

      override def onmessage(evt: MessageEvent): js.Any = {
        source.onTextMessage(evt.data.asInstanceOf[String])
      }
    }
    source
  }

  private def queryHeader(req: Req): js.Object = {
    js.Dynamic.literal("query" -> Json.encode(req).toUtf8String)
  }
}
