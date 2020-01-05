package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.api.{Encoding, ErrorProtocol}
import msocket.api.Encoding.{CborByteBuffer, JsonText}

object RSocketExtensions {

  implicit class RSocketEncoding(encoding: Encoding[_]) {
    def payload[T: Encoder](input: T): Payload = encoding match {
      case CborByteBuffer => DefaultPayload.create(CborByteBuffer.encode(input))
      case JsonText       => DefaultPayload.create(JsonText.encode(input))
      case _              => throw new RuntimeException(s"rsocket transport does not support $encoding encoding")
    }

    def response[Res: Decoder, Req: ErrorProtocol](payload: Payload): Res = encoding match {
      case CborByteBuffer => CborByteBuffer.decodeWithError[Res, Req](payload.getData)
      case JsonText       => JsonText.decodeWithError[Res, Req](payload.getDataUtf8)
      case _              => throw new RuntimeException(s"rsocket transport does not support $encoding encoding")
    }

    def request[Req: Decoder](payload: Payload): Req = encoding match {
      case CborByteBuffer => CborByteBuffer.decode[Req](payload.getData)
      case JsonText       => JsonText.decode[Req](payload.getDataUtf8)
      case _              => throw new RuntimeException(s"rsocket transport does not support $encoding encoding")
    }
  }

}
