package mscoket.impl.ws

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import mscoket.impl.StreamExtensions
import mscoket.impl.ws.Encoding.JsonText

trait WebsocketStreamExtensions extends StreamExtensions[String] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[String, Mat] = {
    input.map(JsonText.encodeText[T])
  }
}
