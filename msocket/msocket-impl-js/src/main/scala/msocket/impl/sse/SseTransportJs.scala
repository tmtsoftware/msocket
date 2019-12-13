package msocket.impl.sse

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.impl.TransportJs

class SseTransportJs[Req: Encoder: ErrorProtocol](uri: String) extends TransportJs[Req](new SseConnector[Req](uri))
