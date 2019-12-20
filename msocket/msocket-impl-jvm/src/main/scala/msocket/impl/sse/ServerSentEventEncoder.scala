package msocket.impl.sse

import akka.http.scaladsl.model.sse.ServerSentEvent
import io.bullet.borer.Encoder
import msocket.api.Encoding.JsonText
import msocket.api.ErrorProtocol
import msocket.impl.MessageEncoder

class ServerSentEventEncoder[Req: ErrorProtocol] extends MessageEncoder[Req, ServerSentEvent] {
  override def encode[Res: Encoder](response: Res): ServerSentEvent = ServerSentEvent(JsonText.encode(response))
}
