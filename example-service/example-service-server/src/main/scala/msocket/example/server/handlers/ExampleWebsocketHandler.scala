package msocket.example.server.handlers

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import msocket.api.ContentType
import msocket.impl.ws.WebsocketHandler

class ExampleWebsocketHandler(exampleApi: ExampleApi, contentType: ContentType) extends WebsocketHandler[ExampleRequest](contentType) {
  override def handle(message: ExampleRequest): Source[Message, NotUsed] = message match {
    case Hello(name)    => futureAsStream(exampleApi.hello(name))
    case Square(number) => futureAsStream(exampleApi.square(number))

    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
  }
}
