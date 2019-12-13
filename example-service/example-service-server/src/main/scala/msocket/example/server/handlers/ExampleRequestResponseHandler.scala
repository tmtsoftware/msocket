package msocket.example.server.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{Hello, Square}
import io.rsocket.Payload
import msocket.api.MessageHandler
import msocket.impl.rsocket.server.RSocketStreamExtensions

import scala.concurrent.{ExecutionContext, Future}

class ExampleRequestResponseHandler(exampleApi: ExampleApi)(implicit ec: ExecutionContext)
    extends MessageHandler[ExampleRequest, Future[Payload]]
    with RSocketStreamExtensions {

  override def handle(message: ExampleRequest): Future[Payload] = message match {
    case Hello(name)    => future(exampleApi.hello(name))
    case Square(number) => future(exampleApi.square(number))
    case _              => Future.failed(new RuntimeException("request-response is not supported bu request-stream handler"))
  }
}
