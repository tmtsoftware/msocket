package msocket.impl.rsocket.server

import io.bullet.borer.Dom.Element
import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.models.{ResponseHeaders, ServiceError}
import msocket.api.{ContentType, ErrorProtocol, ResponseEncoder, RequestHandler}
import msocket.impl.rsocket.RSocketExtensions._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This helper class can be extended to define custom RSocket handler in the server which returns [[Future]] of [[Payload]].
 * RSocketResponseHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class RSocketResponseHandler[Req: ErrorProtocol](contentType: ContentType)
    extends ResponseEncoder[Req, Payload]
    with RequestHandler[Req, Future[Payload]] {

  def future[Res: Encoder](response: Future[Res])(implicit ec: ExecutionContext): Future[Payload] = {
    response.map(response => contentType.payload(response, ResponseHeaders())).recover(errorEncoder)
  }

  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Payload = contentType.payload(response, headers)
}

/**
 * to be added
 */
object RSocketResponseHandler {
  val Missing: ContentType => RSocketResponseHandler[Element] = { contentType =>
    new RSocketResponseHandler[Element](contentType)(ErrorProtocol.bind[Element, ServiceError]) {
      override def handle(request: Element): Future[Payload] = Future.failed(new RuntimeException("missing response handler"))
    }
  }
}
