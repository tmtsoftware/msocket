package msocket.impl.sse

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import typings.eventsource.MessageEvent
import typings.eventsource.mod.{EventSourceInitDict, ^ => Sse}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.control.NonFatal

class SseTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext) extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription = {
    val sse = new Sse(uri, EventSourceInitDict(queryHeader(request))) {
      override def onopen(evt: MessageEvent): js.Any = {
        println("connection open")
      }

      override def onmessage(evt: MessageEvent): js.Any = {
        val jsonString = evt.data.asInstanceOf[String]
        if (jsonString != "") {
          try onMessage(JsonText.decodeWithError(jsonString))
          catch {
            case NonFatal(ex) => onError(ex); close()
          }
        }
      }

      override def onerror(evt: MessageEvent): js.Any = {
        println(evt)
      }
    }

    () => sse.close()
  }

  private def queryHeader(req: Req): js.Object = {
    js.Dynamic.literal("query" -> Json.encode(req).toUtf8String)
  }
}
