package msocket.impl.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.{Encoding, MessageHandler}
import msocket.impl.rsocket.RSocketExtensions._

import scala.concurrent.{ExecutionContext, Future}

abstract class RSocketResponseHandler[Req](encoding: Encoding[_]) extends MessageHandler[Req, Future[Payload]] {
  def future[Res: Encoder](response: Future[Res])(implicit ec: ExecutionContext): Future[Payload] = {
    response.map(response => encoding.payload(response))
  }
}
