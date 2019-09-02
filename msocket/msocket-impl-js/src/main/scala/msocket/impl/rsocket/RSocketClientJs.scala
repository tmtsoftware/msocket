package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import msocket.impl.streaming.StreamingClientJs

import scala.concurrent.{ExecutionContext, Future}

class RSocketClientJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext)
    extends StreamingClientJs[Req](new RSocketConnectionFactory[Req](uri)) {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }
}
