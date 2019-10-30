package msocket.impl.rsocket.server

import akka.stream.scaladsl.Source
import io.bullet.borer.{Encoder, Json}
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.impl.StreamExtensions
import msocket.impl.StreamExtensions

trait RSocketStreamExtensions extends StreamExtensions[Payload] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[Payload, Mat] = {
    input.map(x => DefaultPayload.create(Json.encode(x).toByteBuffer))
  }
}
