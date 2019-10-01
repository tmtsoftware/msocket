package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.impl.streaming.StreamingTransportJs

import scala.concurrent.Future

class WebsocketTransportJs[Req: Encoder](uri: String) extends StreamingTransportJs[Req](new WebsocketConnectionFactory[Req](uri)) {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }
}
