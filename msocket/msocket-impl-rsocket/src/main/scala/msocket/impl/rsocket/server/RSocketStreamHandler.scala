package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.rsocket.Payload
import msocket.api.{ContentType, ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

/**
 * This helper class can be extended to define custom RSocket handler in the server which returns [[Source]] of [[Payload]].
 * RSocketStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class RSocketStreamHandler[Req: ErrorProtocol](contentType: ContentType)
    extends ServerStreamingSupport[Req, Payload](new RSocketPayloadEncoder[Req](contentType))
    with MessageHandler[Req, Source[Payload, NotUsed]]
