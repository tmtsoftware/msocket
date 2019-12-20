package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.rsocket.Payload
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

abstract class RSocketStreamHandler[Req: ErrorProtocol]
    extends ServerStreamingSupport[Req, Payload](new RSocketPayloadEncoder[Req])
    with MessageHandler[Req, Source[Payload, NotUsed]]
