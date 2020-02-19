package msocket.impl.ws

import akka.http.scaladsl.model.ws.Message
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.StreamHandler

/**
 * This helper class can be extended to define custom  websocket handler in the server which returns [[Source]] of [[Message]].
 * WebsocketHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class WebsocketHandler[Req: ErrorProtocol](contentType: ContentType)
    extends StreamHandler[Req, Message](new WebsocketMessageEncoder[Req](contentType))
