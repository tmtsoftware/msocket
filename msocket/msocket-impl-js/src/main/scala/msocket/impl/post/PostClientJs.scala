package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.impl.streaming.StreamingClientJs
import org.scalajs.dom.experimental.{Fetch, HttpMethod}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

class PostClientJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration)
    extends StreamingClientJs[Req](new PostConnectionFactory[Req](uri)) {
  def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    val request = new FetchRequest {
      method = HttpMethod.POST
      body = Json.encode(req).toUtf8String
      headers = js.Dictionary("content-type" -> "application/json")
    }
    Fetch
      .fetch(uri, request)
      .toFuture
      .flatMap { x =>
        x.text().toFuture.map { y =>
          Json.decode(y.getBytes()).to[Res].value
        }
      }
  }
}
