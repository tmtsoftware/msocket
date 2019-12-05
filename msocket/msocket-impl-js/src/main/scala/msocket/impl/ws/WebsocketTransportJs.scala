package msocket.impl.ws

import io.bullet.borer.Encoder
import msocket.api.ErrorType
import msocket.impl.streaming.StreamingTransportJs

class WebsocketTransportJs[Req: Encoder: ErrorType](uri: String) extends StreamingTransportJs[Req](new WebsocketConnectionFactory[Req](uri))
