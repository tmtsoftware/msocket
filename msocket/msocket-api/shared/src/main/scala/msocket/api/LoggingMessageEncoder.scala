package msocket.api

import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.models.Headers

class LoggingMessageEncoder[Req: ErrorProtocol](action: String => Unit = println) extends MessageEncoder[Req, Unit] {
  override def encode[Res: Encoder](response: Res, headers: Headers): Unit = {
    action(s"Response <-- ${JsonText.encode(response)}")
  }
}
