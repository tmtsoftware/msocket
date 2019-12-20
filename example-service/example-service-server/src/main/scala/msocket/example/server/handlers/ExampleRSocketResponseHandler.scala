package msocket.example.server.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.Hello
import io.rsocket.Payload
import msocket.impl.rsocket.server.RSocketResponseHandler

import scala.concurrent.{ExecutionContext, Future}

class ExampleRSocketResponseHandler(exampleApi: ExampleApi)(implicit ec: ExecutionContext) extends RSocketResponseHandler[ExampleRequest] {
  override def handle(message: ExampleRequest): Future[Payload] = message match {
    case Hello(name) => future(exampleApi.hello(name))
    case _           => Future.failed(new RuntimeException("request-response is not supported bu request-stream handler"))
  }
}
