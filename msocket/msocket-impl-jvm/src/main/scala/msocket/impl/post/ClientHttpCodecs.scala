package msocket.impl.post

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.bullet.borer.Encoder
import msocket.api.Encoding
import msocket.api.Encoding.JsonText
import msocket.impl.CborByteString

trait ClientHttpCodecs extends ServerHttpCodecs {
  def encoding: Encoding[_]

  override implicit def borerToEntityMarshaller[T: Encoder]: ToEntityMarshaller[T] = encoding match {
    case CborByteString => borerCborMarshaller()
    case JsonText       => borerJsonMarshaller()
    case _              => throw new RuntimeException(s"http transport does not support $encoding encoding")
  }
}
