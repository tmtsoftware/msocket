package msocket.js.sse

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ErrorProtocol, Subscription}
import msocket.js.JsTransport
import msocket.portable.Observer
import typings.eventsource.MessageEvent
import typings.eventsource.mod.{EventSourceInitDict, ^ => Sse}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.control.NonFatal

class SseTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext) extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {

    val sse = new Sse(uri, EventSourceInitDict().setHeaders(queryHeader(request))) {

      override def onopen(evt: MessageEvent[_]): js.Any = {
        println("sse connection open")
      }

      override def onmessage(evt: MessageEvent[_]): js.Any = {
        val jsonString = evt.data.asInstanceOf[String]
        if (jsonString != "") {
          try observer.onNext(JsonText.decodeWithError(jsonString))
          catch {
            case NonFatal(ex) =>
              observer.onError(ex)
              close()
          }
        }
      }

      override def onerror(evt: MessageEvent[_]): js.Any = {
        observer.onError(new RuntimeException(s"sse connection error=$evt"))
        close()
      }
    }

    () => sse.close(); observer.onCompleted()
  }

  private def queryHeader(req: Req): js.Object = {
    js.Dynamic.literal("Query" -> Json.encode(req).toUtf8String)
  }
}
