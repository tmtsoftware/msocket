package msocket.example.server.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest.{ExampleRequestResponse, Hello, RandomBag}
import io.rsocket.Payload
import msocket.api.ContentType
import msocket.impl.rsocket.server.RSocketResponseHandler

import scala.concurrent.{ExecutionContext, Future}

/**
 * A RSocket handler that will create routes for RequestResponse interaction model APIs in [[ExampleApi]]
 */
class ExampleRSocketResponseHandler(exampleApi: ExampleApi, contentType: ContentType)(implicit ec: ExecutionContext)
    extends RSocketResponseHandler[ExampleRequestResponse](contentType) {

  override def handle(message: ExampleRequestResponse): Future[Payload] = message match {
    case Hello(name) => future(exampleApi.hello(name))
    case RandomBag   => future(exampleApi.randomBag())
  }
}
