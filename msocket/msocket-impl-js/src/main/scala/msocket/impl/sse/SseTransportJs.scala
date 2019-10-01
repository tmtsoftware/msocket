package msocket.impl.sse

import io.bullet.borer.{Decoder, Encoder}
import msocket.impl.streaming.StreamingTransportJs

import scala.concurrent.Future

class SseTransportJs[Req: Encoder](uri: String) extends StreamingTransportJs[Req](new SseConnectionFactory[Req](uri)) {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }
}
