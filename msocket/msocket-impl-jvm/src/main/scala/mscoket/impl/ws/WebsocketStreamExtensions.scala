package mscoket.impl.ws

import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import mscoket.impl.StreamExtensions
import mscoket.impl.ws.Encoding.JsonText

trait WebsocketStreamExtensions extends StreamExtensions[Message] {
  def encoding: Encoding[_]

  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[Message, Mat] = {
    input.map(encoding.strictMessage[T])
  }
}
