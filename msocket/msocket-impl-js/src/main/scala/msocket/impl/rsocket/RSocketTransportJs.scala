package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import msocket.impl.streaming.StreamingTransportJs

import scala.concurrent.{ExecutionContext, Future}

class RSocketTransportJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext)
    extends StreamingTransportJs[Req](new RSocketConnectionFactory[Req](uri)) {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }
}
