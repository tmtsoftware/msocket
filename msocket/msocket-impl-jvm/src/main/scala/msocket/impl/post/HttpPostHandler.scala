package msocket.impl.post

import akka.http.scaladsl.server.Route
import msocket.api.models.FetchEvent
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

abstract class HttpPostHandler[Req: ErrorProtocol]
    extends ServerStreamingSupport[Req, FetchEvent](new FetchEventEncoder[Req])
    with MessageHandler[Req, Route]
