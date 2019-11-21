package msocket.impl.ws

import io.bullet.borer.Encoder
import msocket.impl.streaming.StreamingTransportJs

class WebsocketTransportJs[Req: Encoder](uri: String) extends StreamingTransportJs[Req](new WebsocketConnectionFactory[Req](uri))
