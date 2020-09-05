package csw.example.impl.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, Hello, RandomBag}
import msocket.jvm.mono.{MonoRequestHandler, MonoResponse}

import scala.concurrent.Future

/**
 * Implements RSocketResponseHandler for all requestResponse messages in the protocol
 * These handlers handle RSocket's requestResponse interaction model and returns a [[Future]] of [[Payload]]
 */
class ExampleMonoRequestHandler(exampleApi: ExampleApi) extends MonoRequestHandler[ExampleRequest] {

  override def handle(message: ExampleRequest): Future[MonoResponse] =
    message match {
      case Hello(name) => future(exampleApi.hello(name))
      case RandomBag   => future(exampleApi.randomBag())
    }
}
