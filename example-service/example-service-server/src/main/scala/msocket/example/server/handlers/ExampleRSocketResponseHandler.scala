package msocket.example.server.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.Hello
import io.rsocket.Payload
import msocket.api.ContentType
import msocket.impl.rsocket.server.RSocketResponseHandler

import scala.concurrent.{ExecutionContext, Future}

class ExampleRSocketResponseHandler(exampleApi: ExampleApi, contentType: ContentType)(implicit ec: ExecutionContext)
    extends RSocketResponseHandler[ExampleRequest](contentType) {

  override def handle(message: ExampleRequest): Future[Payload] = message match {
    case Hello(name) => future(exampleApi.hello(name))
    case _           => Future.failed(new RuntimeException("request-stream is not supported by request-response handler"))
  }
}
