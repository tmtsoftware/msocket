package msocket.impl.rsocket

import io.bullet.borer.Encoder
import msocket.impl.streaming.StreamingTransportJs

import scala.concurrent.ExecutionContext

class RSocketTransportJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext)
    extends StreamingTransportJs[Req](new RSocketConnectionFactory[Req](uri))
