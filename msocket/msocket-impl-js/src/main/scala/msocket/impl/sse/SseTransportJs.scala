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
import scala.util.{Failure, Success, Try}

class SseTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext) extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Try[Option[Res]] => Unit): Subscription = {
    val sse = new Sse(uri, EventSourceInitDict(queryHeader(request))) {
      override def onopen(evt: MessageEvent): js.Any = {
        println("sse connection open")
      }

      override def onmessage(evt: MessageEvent): js.Any = {
        val jsonString = evt.data.asInstanceOf[String]
        if (jsonString != "") {
          try onMessage(Success(Some(JsonText.decodeWithError(jsonString))))
          catch {
            case NonFatal(ex) => onMessage(Failure(ex)); close(); onMessage(Success(None))
          }
        }
      }

      override def onerror(evt: MessageEvent): js.Any = {
        onMessage(Failure(new RuntimeException(s"sse connection error=$evt")))
      }
    }

    () => sse.close(); onMessage(Success(None))
  }

  private def queryHeader(req: Req): js.Object = {
    js.Dynamic.literal("Query" -> Json.encode(req).toUtf8String)
  }
}
