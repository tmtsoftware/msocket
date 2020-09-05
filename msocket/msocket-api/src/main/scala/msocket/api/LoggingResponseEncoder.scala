package msocket.api

import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.models.ResponseHeaders

class LoggingResponseEncoder[Req: ErrorProtocol](action: String => Unit = println) extends ResponseEncoder[Req, Unit] {
  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Unit = {
    action(s"Response <-- ${JsonText.encode(response)}")
  }
}
