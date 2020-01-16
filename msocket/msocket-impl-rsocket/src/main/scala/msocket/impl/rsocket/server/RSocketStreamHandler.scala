package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.rsocket.Payload
import msocket.api.{ContentType, ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

abstract class RSocketStreamHandler[Req: ErrorProtocol](contentType: ContentType)
    extends ServerStreamingSupport[Req, Payload](new RSocketPayloadEncoder[Req](contentType))
    with MessageHandler[Req, Source[Payload, NotUsed]]
