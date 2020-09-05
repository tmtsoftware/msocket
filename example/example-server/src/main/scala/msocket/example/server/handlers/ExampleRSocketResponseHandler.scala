package msocket.example.server.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, Hello, RandomBag}
import io.rsocket.Payload
import msocket.api.ContentType
import csw.example.api.protocol.ExampleCodecs._
import msocket.rsocket.server.RSocketResponseHandler

import scala.concurrent.{ExecutionContext, Future}

/**
 * Implements RSocketResponseHandler for all requestResponse messages in the protocol
 * These handlers handle RSocket's requestResponse interaction model and returns a [[Future]] of [[Payload]]
 */
class ExampleRSocketResponseHandler(exampleApi: ExampleApi, contentType: ContentType)(implicit ec: ExecutionContext)
    extends RSocketResponseHandler[ExampleRequest](contentType) {

  override def handle(message: ExampleRequest): Future[Payload] =
    message match {
      case Hello(name) => future(exampleApi.hello(name))
      case RandomBag   => future(exampleApi.randomBag())
    }
}
