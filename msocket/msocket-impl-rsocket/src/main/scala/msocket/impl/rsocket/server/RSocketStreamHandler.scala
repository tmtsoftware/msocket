package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.rsocket.Payload
import msocket.api.{Encoding, ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

abstract class RSocketStreamHandler[Req: ErrorProtocol](encoding: Encoding[_])
    extends ServerStreamingSupport[Req, Payload](new RSocketPayloadEncoder[Req](encoding))
    with MessageHandler[Req, Source[Payload, NotUsed]]
