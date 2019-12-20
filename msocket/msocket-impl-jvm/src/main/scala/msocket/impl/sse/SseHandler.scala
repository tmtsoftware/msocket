package msocket.impl.sse

import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

abstract class SseHandler[Req: ErrorProtocol]
    extends ServerStreamingSupport[Req, ServerSentEvent](new ServerSentEventEncoder[Req])
    with MessageHandler[Req, Route]
