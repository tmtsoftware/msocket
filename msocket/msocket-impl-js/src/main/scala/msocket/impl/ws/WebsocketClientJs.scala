package msocket.impl.ws

import io.bullet.borer.{Decoder, Encoder}
import msocket.impl.streaming.StreamingClientJs

import scala.concurrent.Future

class WebsocketClientJs[Req: Encoder](uri: String) extends StreamingClientJs[Req](new WebsocketConnectionFactory[Req](uri)) {

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    requestResponseWithDelay(request)
  }
}
