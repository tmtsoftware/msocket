package msocket.impl.rsocket

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.impl.TransportJs

import scala.concurrent.ExecutionContext

class RSocketTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext)
    extends TransportJs[Req](new RSocketConnector[Req](uri))
