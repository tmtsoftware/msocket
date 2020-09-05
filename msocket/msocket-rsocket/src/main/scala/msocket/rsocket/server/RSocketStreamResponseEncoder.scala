package msocket.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.models.ResponseHeaders
import msocket.api.{ContentType, ErrorProtocol}
import msocket.jvm.stream.StreamResponseEncoder
import msocket.rsocket.RSocketExtensions._
import msocket.security.AccessController

/**
 * This helper class can be extended to define custom RSocket handler in the server which returns [[Source]] of [[Payload]].
 * RSocketStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
class RSocketStreamResponseEncoder[Req: ErrorProtocol](contentType: ContentType, val accessController: AccessController)
    extends StreamResponseEncoder[Req, Payload] {
  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Payload = contentType.payload(response, headers)
}
