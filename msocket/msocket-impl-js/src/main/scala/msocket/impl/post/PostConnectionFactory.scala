package msocket.impl.post

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{Closeable, ConnectedSource, ConnectionFactory}
import org.scalajs.dom.experimental.{Fetch, HttpMethod, ReadableStreamReader}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js
import scala.scalajs.js.{JSON, timers}

class PostConnectionFactory[Req: Encoder](uri: String)(implicit ec: ExecutionContext, timeout: FiniteDuration) extends ConnectionFactory {

  override def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S = {
    val request = new FetchRequest {
      method = HttpMethod.POST
      body = Json.encode(req).toUtf8String
      headers = js.Dictionary("content-type" -> "application/json")
    }
    Fetch
      .fetch(uri, request)
      .toFuture
      .foreach { response =>
        val reader: ReadableStreamReader[js.Object] = new CanNdJsonStream(response.body).getReader()

        source.closeable = new Closeable {
          override def closeStream(): Unit = reader.cancel("cancelled")
        }

        def read(): Unit = {
          reader.read().toFuture.foreach { chunk =>
            if (!chunk.done) {
              source.onTextMessage(JSON.stringify(chunk.value))
              timers.setTimeout(timeout) {
                read()
              }
            }
          }
        }

        read()
      }
    source
  }
}
