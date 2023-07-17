package msocket.http.post

import org.apache.pekko.http.scaladsl.server.Route
import msocket.api.ErrorProtocol

/**
 * This helper class can be extended to define custom HTTP routes[[Route]] handler in the server.
 * HttpPostHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class HttpPostHandler[Req: ErrorProtocol] {
  def handle(request: Req): Route
}
