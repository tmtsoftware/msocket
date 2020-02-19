package msocket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import msocket.api.{ContentType, ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

/**
 * This helper class can be extended to define custom  websocket handler in the server which returns [[Source]] of [[Message]].
 * WebsocketHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class WebsocketHandler[Req: ErrorProtocol](contentType: ContentType)
    extends ServerStreamingSupport[Req, Message](new WebsocketMessageEncoder[Req](contentType))
    with MessageHandler[Req, Source[Message, NotUsed]]
