package msocket.impl.post

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{Closeable, ConnectedSource, ConnectionFactory}
import org.scalajs.dom.experimental.{Fetch, HttpMethod}
import typings.std.ReadableStream

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.JSON

class PostConnectionFactory[Req: Encoder](uri: String)(implicit ec: ExecutionContext) extends ConnectionFactory {

  override def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S = {
    val request = new FetchRequest {
      method = HttpMethod.POST
      body = Json.encode(req).toUtf8String
      headers = js.Dictionary("content-type" -> "application/json")
    }
    Fetch
      .fetch(uri, request)
      .toFuture
      .foreach { x =>
        val reader = new CanNdJsonStream(x.body).getReader()

        source.closeable = new Closeable {
          override def closeStream(): Unit = reader.cancel("cancelled")
        }

        def read(): Unit = {
          val future = reader.read().toFuture
          future.foreach { chunk =>
            if (!chunk.done) {
              source.onTextMessage(JSON.stringify(chunk.value))
              read()
            }
          }
        }

        read()
      }
    source
  }
}
