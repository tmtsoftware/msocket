package msocket.impl.ws

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.impl.streaming.StreamingTransportJs

class WebsocketTransportJs[Req: Encoder: ErrorProtocol](uri: String) extends StreamingTransportJs[Req](new WebsocketConnector[Req](uri))
