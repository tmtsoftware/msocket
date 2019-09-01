package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.impl.streaming.StreamingClientJs

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class PostClientJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration)
    extends StreamingClientJs[Req](new PostConnectionFactory[Req](uri)) {

  def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    FetchHelper.postRequest(uri, req).flatMap { response =>
      response.text().toFuture.map { data =>
        Json.decode(data.getBytes()).to[Res].value
      }
    }
  }

}
