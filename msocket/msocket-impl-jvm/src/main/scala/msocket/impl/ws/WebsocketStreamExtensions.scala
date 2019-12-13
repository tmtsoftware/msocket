package msocket.impl.ws

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.Encoding
import msocket.impl.ws.EncodingExtensions.EncodingForMessage
import msocket.impl.StreamExtensions

trait WebsocketStreamExtensions extends StreamExtensions[Message] {
  def encoding: Encoding[_]

  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[Message, NotUsed] = {
    input
      .map(encoding.strictMessage[T])
      .mapMaterializedValue(_ => NotUsed)
  }
}
