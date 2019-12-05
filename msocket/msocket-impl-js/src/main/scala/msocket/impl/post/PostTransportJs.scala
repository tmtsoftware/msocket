package msocket.impl.post

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.ErrorProtocol
import msocket.api.models.ServiceError
import msocket.impl.streaming.StreamingTransportJs

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class PostTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration)
    extends StreamingTransportJs[Req](new PostConnectionFactory[Req](uri)) {

  override def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    FetchHelper.postRequest(uri, req).flatMap { response =>
      response.text().toFuture.map { data =>
        Json
          .decode(data.getBytes())
          .to[Res]
          .valueTry
          .getOrElse {
            throw Json.decode(data.getBytes()).to[ServiceError].value
          }
      }
    }
  }

}
