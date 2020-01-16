package msocket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import msocket.api.{ContentType, ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

abstract class WebsocketHandler[Req: ErrorProtocol](contentType: ContentType)
    extends ServerStreamingSupport[Req, Message](new WebsocketMessageEncoder[Req](contentType))
    with MessageHandler[Req, Source[Message, NotUsed]]
