package msocket.impl.sse

import io.bullet.borer.Encoder
import msocket.impl.streaming.StreamingTransportJs

class SseTransportJs[Req: Encoder](uri: String) extends StreamingTransportJs[Req](new SseConnectionFactory[Req](uri))
