package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.models.MSocketException
import msocket.impl.streaming.StreamingTransportJs

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class PostTransportJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration)
    extends StreamingTransportJs[Req](new PostConnectionFactory[Req](uri)) {

  override def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    FetchHelper.postRequest(uri, req).flatMap { response =>
      response.text().toFuture.map { data =>
        Json
          .decode(data.getBytes())
          .to[Res]
          .valueTry
          .getOrElse {
            throw Json.decode(data.getBytes()).to[MSocketException].value
          }
      }
    }
  }

}
