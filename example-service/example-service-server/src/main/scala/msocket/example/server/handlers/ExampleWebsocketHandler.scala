package msocket.example.server.handlers

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, RandomBag, RandomBagStream, Square}
import msocket.api.ContentType
import msocket.impl.ws.WebsocketHandler

/**
 * A Websocket handler that will create routes for defined APIs in [[ExampleApi]]
 */
class ExampleWebsocketHandler(exampleApi: ExampleApi, contentType: ContentType) extends WebsocketHandler[ExampleRequest](contentType) {
  override def handle(message: ExampleRequest): Source[Message, NotUsed] = message match {
    case Hello(name)    => futureAsStream(exampleApi.hello(name))
    case Square(number) => futureAsStream(exampleApi.square(number))
    case RandomBag      => futureAsStream(exampleApi.randomBag())

    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
    case RandomBagStream         => stream(exampleApi.randomBagStream())
  }
}
