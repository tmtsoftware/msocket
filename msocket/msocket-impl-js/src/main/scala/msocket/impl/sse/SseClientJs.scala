package msocket.impl.sse

import io.bullet.borer.{Decoder, Encoder}
import msocket.impl.streaming.StreamingClientJs

import scala.concurrent.Future

class SseClientJs[Req: Encoder](uri: String) extends StreamingClientJs[Req](new SseConnectionFactory[Req](uri)) {
  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }
}
