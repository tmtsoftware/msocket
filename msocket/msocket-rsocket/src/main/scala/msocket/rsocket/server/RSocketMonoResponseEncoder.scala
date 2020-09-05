package msocket.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.models.ResponseHeaders
import msocket.api.{ContentType, ErrorProtocol}
import msocket.jvm.mono.MonoResponseEncoder
import msocket.rsocket.RSocketExtensions._
import msocket.security.AccessController

import scala.concurrent.ExecutionContext

class RSocketMonoResponseEncoder[Req: ErrorProtocol](contentType: ContentType, val accessController: AccessController)(implicit
    ec: ExecutionContext
) extends MonoResponseEncoder[Req, Payload] {
  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Payload = contentType.payload(response, headers)
}
