package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.impl.TransportJs

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class PostTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration)
    extends TransportJs[Req](new PostConnector[Req](uri))
