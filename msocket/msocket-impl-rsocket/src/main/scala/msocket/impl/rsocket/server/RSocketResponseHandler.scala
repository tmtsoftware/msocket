package msocket.impl.rsocket.server

import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.{ContentType, MessageHandler}
import msocket.impl.rsocket.RSocketExtensions._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This helper class can be extended to define custom RSocket handler in the server which returns [[Future]] of [[Payload]].
 * RSocketResponseHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class RSocketResponseHandler[Req](contentType: ContentType) extends MessageHandler[Req, Future[Payload]] {
  def future[Res: Encoder](response: Future[Res])(implicit ec: ExecutionContext): Future[Payload] = {
    response.map(response => contentType.payload(response))
  }
}
