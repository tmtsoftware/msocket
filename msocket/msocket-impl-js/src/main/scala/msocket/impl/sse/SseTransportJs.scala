package msocket.impl.sse

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.impl.streaming.StreamingTransportJs

class SseTransportJs[Req: Encoder: ErrorProtocol](uri: String) extends StreamingTransportJs[Req](new SseConnector[Req](uri))
