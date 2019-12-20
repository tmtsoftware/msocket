package msocket.impl.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.api.Encoding.CborByteBuffer
import msocket.api.MessageHandler

import scala.concurrent.{ExecutionContext, Future}

abstract class RSocketResponseHandler[Req] extends MessageHandler[Req, Future[Payload]] {
  def future[Res: Encoder](response: Future[Res])(implicit ec: ExecutionContext): Future[Payload] = {
    response.map(response => DefaultPayload.create(CborByteBuffer.encode(response)))
  }
}
