package msocket.js.sse

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.js.JsTransport
import msocket.portable.Observer
import typings.microsoftFetchEventSource.fetchMod.FetchEventSourceInit
import typings.microsoftFetchEventSource.mod.fetchEventSource
import typings.std.global.AbortController

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.util.control.NonFatal

class SseTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext) extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {

    val controller = new AbortController()

    val fetchEventSourceInit = new FetchEventSourceInit {
      signal = controller.signal

      method = "POST"

      body = JsonText.encode(request)

      headers = js.Array(js.Array("Content-Type", ContentType.Json.mimeType))

      onopen = js.defined { res =>
        println(s"sse connection open with status: ${res.status}")
        Future.successful(()).toJSPromise
      }

      onmessage = js.defined { evt =>
        val jsonString = evt.data.asInstanceOf[String]
        if (jsonString != "") {
          try observer.onNext(JsonText.decodeWithError(jsonString))
          catch {
            case NonFatal(ex) =>
              observer.onError(ex)
              controller.abort()
          }
        }
      }

      onerror = js.defined { evt =>
        observer.onError(new RuntimeException(s"sse connection error=$evt"))
        controller.abort()
      }
    }

    fetchEventSource(uri, fetchEventSourceInit)

    () => controller.abort(); observer.onCompleted()
  }
}
