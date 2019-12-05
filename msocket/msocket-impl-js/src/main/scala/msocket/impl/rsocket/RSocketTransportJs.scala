package msocket.impl.rsocket

import io.bullet.borer.Encoder
import msocket.api.ErrorType
import msocket.impl.streaming.StreamingTransportJs

import scala.concurrent.ExecutionContext

class RSocketTransportJs[Req: Encoder: ErrorType](uri: String)(implicit ec: ExecutionContext)
    extends StreamingTransportJs[Req](new RSocketConnectionFactory[Req](uri))
