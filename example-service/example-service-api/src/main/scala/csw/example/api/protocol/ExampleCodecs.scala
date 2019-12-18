package csw.example.api.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.CompactMapBasedCodecs
import msocket.api.ErrorProtocol

object ExampleCodecs extends ExampleCodecs
trait ExampleCodecs {
  implicit lazy val exampleRequestCodec: Codec[ExampleRequest]                 = CompactMapBasedCodecs.deriveAllCodecs
  implicit lazy val exampleErrorCodec: Codec[ExampleError]                     = CompactMapBasedCodecs.deriveAllCodecs
  implicit lazy val exampleRequestErrorProtocol: ErrorProtocol[ExampleRequest] = ErrorProtocol.bind[ExampleRequest, ExampleError]
}
