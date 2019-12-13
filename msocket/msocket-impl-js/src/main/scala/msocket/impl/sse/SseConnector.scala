package msocket.impl.sse

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.Encoding.JsonText
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.Connector
import typings.eventsource.MessageEvent
import typings.eventsource.eventsourceMod.{EventSourceInitDict, ^ => Sse}

import scala.concurrent.Future
import scala.scalajs.js

class SseConnector[Req: Encoder: ErrorProtocol](uri: String) extends Connector[Req] {

  override def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not yet supported for this transport"))
  }

  override def requestStream[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription = {
    val sse = new Sse(uri, EventSourceInitDict(queryHeader(req))) {
      override def onopen(evt: MessageEvent): js.Any = {
        println("connection open")
      }

      override def onmessage(evt: MessageEvent): js.Any = {
        val jsonString = evt.data.asInstanceOf[String]
        if (jsonString != "") {
          onMessage(JsonText.decodeWithError(jsonString))
        }
      }
    }

    () => sse.close()
  }

  private def queryHeader(req: Req): js.Object = {
    js.Dynamic.literal("query" -> Json.encode(req).toUtf8String)
  }
}
