package msocket.example.server.handlers

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleProtocol._
import msocket.impl.sse.SseHandler

/**
 * Implements SseHandler for requestStream messages in the protocol
 * These handlers receive GET requests and responds with [[Source]] of [[ServerSentEvent]]
 */
class ExampleSseHandler(exampleApi: ExampleApi) extends SseHandler[ExampleStreamRequest] {

  override def handle(request: ExampleStreamRequest): Source[ServerSentEvent, NotUsed] =
    request match {
      case Square(number)          => stream(exampleApi.square(number))
      case HelloStream(name)       => stream(exampleApi.helloStream(name))
      case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
      case RandomBagStream         => stream(exampleApi.randomBagStream())
    }
}
