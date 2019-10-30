package msocket.impl.post

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.bullet.borer.Encoder
import msocket.impl.Encoding
import msocket.impl.Encoding.{CborBinary, JsonText}

trait ClientHttpCodecs extends ServerHttpCodecs {
  def encoding: Encoding[_]

  override implicit def borerToEntityMarshaller[T: Encoder]: ToEntityMarshaller[T] = encoding match {
    case CborBinary => borerCborMarshaller()
    case JsonText   => borerJsonMarshaller()
  }
}
