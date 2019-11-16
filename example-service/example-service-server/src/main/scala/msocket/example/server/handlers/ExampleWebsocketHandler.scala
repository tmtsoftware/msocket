package msocket.example.server.handlers

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import msocket.api.MessageHandler
import msocket.impl.Encoding
import msocket.impl.ws.WebsocketStreamExtensions

class ExampleWebsocketHandler(exampleApi: ExampleApi, val encoding: Encoding[_])(implicit actorSystem: ActorSystem[_])
    extends MessageHandler[ExampleRequest, Source[Message, NotUsed]]
    with WebsocketStreamExtensions {

  override def handle(message: ExampleRequest): Source[Message, NotUsed] = message match {
    case Hello(name)    => futureAsStream(exampleApi.hello(name))
    case Square(number) => futureAsStream(exampleApi.square(number))

    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => streamWithStatus(exampleApi.getNumbers(divisibleBy))
  }
}
