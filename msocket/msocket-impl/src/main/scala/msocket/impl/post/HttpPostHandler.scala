package msocket.impl.post

import akka.http.scaladsl.server.Route
import msocket.api.{ErrorProtocol, RequestHandler}

/**
 * This helper class can be extended to define custom HTTP routes[[Route]] handler in the server.
 * HttpPostHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class HttpPostHandler[Req: ErrorProtocol] extends RequestHandler[Req, Route]
