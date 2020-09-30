package csw.example.api.protocol

import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
import io.bullet.borer.derivation.MapBasedCodecs
import io.bullet.borer.{AdtEncodingStrategy, Codec}
import msocket.api.ErrorProtocol

/**
 * Borer Codecs
 */
object ExampleCodecs extends ExampleCodecs
trait ExampleCodecs {
  implicit val flatAdtEncoding: AdtEncodingStrategy = AdtEncodingStrategy.flat()

  /** Codec for message protocol */
  implicit lazy val exampleRequestResponseCodec: Codec[ExampleRequest]     = MapBasedCodecs.deriveAllCodecs
  implicit lazy val exampleRequestStreamCodec: Codec[ExampleStreamRequest] = MapBasedCodecs.deriveAllCodecs

  /** Codec for error protocol */
  implicit lazy val exampleErrorCodec: Codec[ExampleError] = MapBasedCodecs.deriveAllCodecs

  /**
   * Bind error protocol to the message protocol
   * This is required so that domain specific errors are correctly
   * serialized without error types being known at compile time
   */
  implicit lazy val exampleRequestResponseErrorProtocol: ErrorProtocol[ExampleRequest]     =
    ErrorProtocol.bind[ExampleRequest, ExampleError]
  implicit lazy val exampleRequestStreamErrorProtocol: ErrorProtocol[ExampleStreamRequest] =
    ErrorProtocol.bind[ExampleStreamRequest, ExampleError]
}
