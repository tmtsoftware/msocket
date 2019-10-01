package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.impl.streaming.{ConnectedSource, ConnectionFactory}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers

class PostConnectionFactory[Req: Encoder](uri: String)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration)
    extends ConnectionFactory {

  override def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S = {
    FetchHelper.postRequest(uri, req).foreach { response =>
      val reader = new CanNdJsonStream(response.body).getReader()

      source.subscription = () => reader.cancel("cancelled")

      def read(): Unit = {
        reader.read().toFuture.foreach { chunk =>
          if (!chunk.done) {
            val jsonString = FetchEventJs(chunk.value).data
            if (jsonString != "") {
              source.onTextMessage(jsonString)
            }
            timers.setTimeout(streamingDelay) {
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
