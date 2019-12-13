package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import org.scalajs.dom.experimental.ReadableStreamReader

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.timers

class HttpPostTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration)
    extends JsTransport[Req] {

  override def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    FetchHelper.postRequest(uri, req).flatMap { response =>
      response.text().toFuture.map(data => JsonText.decodeWithError(data))
    }
  }

  override def requestStream[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription = {
    val readerF: Future[ReadableStreamReader[js.Object]] = FetchHelper.postRequest(uri, req).map { response =>
      val reader = new CanNdJsonStream(response.body).getReader()
      def read(): Unit = {
        reader.read().toFuture.foreach { chunk =>
          if (!chunk.done) {
            val jsonString = FetchEventJs(chunk.value).data
            if (jsonString != "") {
              onMessage(JsonText.decodeWithError(jsonString))
            }
            timers.setTimeout(streamingDelay) {
              read()
            }
          }
        }
      }

      read()

      reader
    }

    () => readerF.foreach(_.cancel("cancelled"))
  }

}
