package msocket.api

import io.bullet.borer.Encoder
import msocket.api.Encoding.JsonText

class LoggingMessageEncoder[Req: ErrorProtocol](action: String => Unit = println) extends MessageEncoder[Req, Unit] {
  override def encode[Res: Encoder](response: Res): Unit = {
    action(s"Response <-- ${JsonText.encode(response)}")
  }
}
