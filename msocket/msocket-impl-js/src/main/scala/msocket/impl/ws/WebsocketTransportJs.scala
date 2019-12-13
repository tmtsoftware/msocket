package msocket.impl.ws

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.impl.TransportJs

class WebsocketTransportJs[Req: Encoder: ErrorProtocol](uri: String) extends TransportJs[Req](new WebsocketConnector[Req](uri))
