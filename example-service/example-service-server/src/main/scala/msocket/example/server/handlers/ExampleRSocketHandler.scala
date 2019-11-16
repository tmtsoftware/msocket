package msocket.example.server.handlers

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import io.rsocket.Payload
import msocket.api.MessageHandler
import msocket.impl.rsocket.server.RSocketStreamExtensions

class ExampleRSocketHandler(exampleApi: ExampleApi)(implicit actorSystem: ActorSystem[_])
    extends MessageHandler[ExampleRequest, Source[Payload, NotUsed]]
    with RSocketStreamExtensions {

  override def handle(message: ExampleRequest): Source[Payload, NotUsed] = message match {
    case Hello(name)    => futureAsStream(exampleApi.hello(name))
    case Square(number) => futureAsStream(exampleApi.square(number))

    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => streamWithStatus(exampleApi.getNumbers(divisibleBy))
  }
}
