package msocket.impl.rsocket.server

import io.rsocket.Payload
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.StreamHandler

/**
 * This helper class can be extended to define custom RSocket handler in the server which returns [[Source]] of [[Payload]].
 * RSocketStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class RSocketStreamHandler[Req: ErrorProtocol](contentType: ContentType)
    extends StreamHandler[Req, Payload](new RSocketPayloadEncoder[Req](contentType))
