package msocket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import msocket.api.{Encoding, ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

abstract class WebsocketHandler[Req: ErrorProtocol](encoding: Encoding[_])
    extends ServerStreamingSupport[Req, Message](new WebsocketMessageEncoder[Req](encoding))
    with MessageHandler[Req, Source[Message, NotUsed]]
