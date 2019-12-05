package msocket.impl.sse

import io.bullet.borer.Encoder
import msocket.api.ErrorType
import msocket.impl.streaming.StreamingTransportJs

class SseTransportJs[Req: Encoder: ErrorType](uri: String) extends StreamingTransportJs[Req](new SseConnectionFactory[Req](uri))
